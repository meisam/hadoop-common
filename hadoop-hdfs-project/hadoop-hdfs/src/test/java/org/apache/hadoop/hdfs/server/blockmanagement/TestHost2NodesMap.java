/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hdfs.server.blockmanagement;

import org.apache.hadoop.hdfs.protocol.DatanodeID;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestHost2NodesMap {
  private Host2NodesMap map = new Host2NodesMap();
  private DatanodeDescriptor dataNodes[];
  
  @Before
  public void setup() {
    dataNodes = new DatanodeDescriptor[] {
      new DatanodeDescriptor(new DatanodeID("1.1.1.1", 5020), "/d1/r1"),
      new DatanodeDescriptor(new DatanodeID("2.2.2.2", 5020), "/d1/r1"),
      new DatanodeDescriptor(new DatanodeID("3.3.3.3", 5020), "/d1/r2"),
      new DatanodeDescriptor(new DatanodeID("3.3.3.3", 5030), "/d1/r2"),
    };
    for (DatanodeDescriptor node : dataNodes) {
      map.add(node);
    }
    map.add(null);
  }
  
  @Test
  public void testContains() throws Exception {
    DatanodeDescriptor nodeNotInMap =
      new DatanodeDescriptor(new DatanodeID("3.3.3.3", 5040), "/d1/r4");
    for (int i = 0; i < dataNodes.length; i++) {
      assertTrue(map.contains(dataNodes[i]));
    }
    assertFalse(map.contains(null));
    assertFalse(map.contains(nodeNotInMap));
  }

  @Test
  public void testGetDatanodeByHost() throws Exception {
    assertEquals(map.getDatanodeByHost("1.1.1.1"), dataNodes[0]);
    assertEquals(map.getDatanodeByHost("2.2.2.2"), dataNodes[1]);
    DatanodeDescriptor node = map.getDatanodeByHost("3.3.3.3");
    assertTrue(node == dataNodes[2] || node == dataNodes[3]);
    assertNull(map.getDatanodeByHost("4.4.4.4"));
  }

  @Test
  public void testRemove() throws Exception {
    DatanodeDescriptor nodeNotInMap =
      new DatanodeDescriptor(new DatanodeID("3.3.3.3", 5040), "/d1/r4");
    assertFalse(map.remove(nodeNotInMap));
    
    assertTrue(map.remove(dataNodes[0]));
    assertTrue(map.getDatanodeByHost("1.1.1.1.")==null);
    assertTrue(map.getDatanodeByHost("2.2.2.2")==dataNodes[1]);
    DatanodeDescriptor node = map.getDatanodeByHost("3.3.3.3");
    assertTrue(node==dataNodes[2] || node==dataNodes[3]);
    assertNull(map.getDatanodeByHost("4.4.4.4"));
    
    assertTrue(map.remove(dataNodes[2]));
    assertNull(map.getDatanodeByHost("1.1.1.1"));
    assertEquals(map.getDatanodeByHost("2.2.2.2"), dataNodes[1]);
    assertEquals(map.getDatanodeByHost("3.3.3.3"), dataNodes[3]);
    
    assertTrue(map.remove(dataNodes[3]));
    assertNull(map.getDatanodeByHost("1.1.1.1"));
    assertEquals(map.getDatanodeByHost("2.2.2.2"), dataNodes[1]);
    assertNull(map.getDatanodeByHost("3.3.3.3"));
    
    assertFalse(map.remove(null));
    assertTrue(map.remove(dataNodes[1]));
    assertFalse(map.remove(dataNodes[1]));
  }

}