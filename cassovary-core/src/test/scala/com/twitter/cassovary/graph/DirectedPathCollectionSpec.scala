/*
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.twitter.cassovary.graph

import com.twitter.cassovary.util.FastUtilUtils
import it.unimi.dsi.fastutil.objects.Object2IntMap
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DirectedPathCollectionSpec extends WordSpec with Matchers {


  "DirectedPathCollection" when {
    "out of the same node" should {

      val testPathIds = Array(10, 11, 12, 14, 15, 11, 14, 0)

      def getPath(ids: Int*) = DirectedPath(ids.toArray.map {
        testPathIds(_)
      })

      def addPath(coll: DirectedPathCollection, idsInPath: List[Int]) {
        idsInPath foreach { id =>
          coll.appendToCurrentPath(testPathIds(id))
        }
      }

      "have 1 path sequence with 3 nodes" in {
        val paths = new DirectedPathCollection

        (1 to 2) foreach { times =>
          paths.resetCurrentPath()
          addPath(paths, List(0, 1, 2))

          pathMapToSeq(paths.topPathsTill(testPathIds(0), 10)) shouldEqual Array((getPath(0), times)).toSeq
          pathMapToSeq(paths.topPathsTill(testPathIds(1), 10)) shouldEqual Array((getPath(0, 1), times)).toSeq
          pathMapToSeq(paths.topPathsTill(testPathIds(2), 10)) shouldEqual Array((getPath(0, 1, 2), times)).toSeq

          List(0, 1, 2) foreach { id =>
            paths.numUniquePathsTill(testPathIds(id)) shouldEqual 1
          }

          paths.totalNumPaths shouldEqual 3
        }
      }

      "have 3 paths with 4 nodes" in {
        val paths = new DirectedPathCollection
        addPath(paths, List(0, 1, 2))
        paths.resetCurrentPath()
        addPath(paths, List(1, 2, 3))
        paths.resetCurrentPath()
        addPath(paths, List(1, 0, 3, 2, 3))
        paths.resetCurrentPath()
        addPath(paths, List(1, 2, 3))

        pathMapToSeq(paths.topPathsTill(testPathIds(0), 10)) shouldEqual Array(
          (getPath(0), 1),
          (getPath(1, 0), 1)
        ).toSeq
        pathMapToSeq(paths.topPathsTill(testPathIds(1), 10)) shouldEqual Array(
          (getPath(1), 3),
          (getPath(0, 1), 1)
        ).toSeq
        pathMapToSeq(paths.topPathsTill(testPathIds(2), 10)) shouldEqual Array(
          (getPath(1, 2), 2),
          (getPath(0, 1, 2), 1),
          (getPath(1, 0, 3, 2), 1)
        ).toSeq
        pathMapToSeq(paths.topPathsTill(testPathIds(3), 10)) shouldEqual Array(
          (getPath(1, 2, 3), 2),
          (getPath(1, 0, 3), 1),
          (getPath(1, 0, 3, 2, 3), 1)
        ).toSeq

        pathMapToSeq(paths.topPathsTill(testPathIds(2), 3)) shouldEqual Array(
          (getPath(1, 2), 2),
          (getPath(0, 1, 2), 1),
          (getPath(1, 0, 3, 2), 1)
        ).toSeq

        paths.numUniquePathsTill(testPathIds(0)) shouldEqual 2
        paths.numUniquePathsTill(testPathIds(1)) shouldEqual 2
        paths.numUniquePathsTill(testPathIds(2)) shouldEqual 3
        paths.numUniquePathsTill(testPathIds(3)) shouldEqual 3

        paths.totalNumPaths shouldEqual 10
      }
    }
  }


  def pathMapToSeq(map: Object2IntMap[DirectedPath]) = {
    FastUtilUtils.object2IntMapToArray(map).toSeq
  }
}
