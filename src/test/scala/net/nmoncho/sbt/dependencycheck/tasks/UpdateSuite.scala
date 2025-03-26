/*
 * Copyright (c) 2025 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.nmoncho.sbt.dependencycheck.tasks

import org.mockito.Mockito._
import org.owasp.dependencycheck.Engine
import sbt.Logger

class UpdateSuite extends munit.FunSuite {

  test("The Update task should delegate update to the Owasp Engine") {
    implicit val log: Logger = Logger.Null

    val engine = mock(classOf[Engine])

    Update(engine)

    verify(engine, atLeastOnce()).doUpdates()
  }

  test("The Update task should report failures when delegating update to the Owasp Engine") {
    implicit val log: Logger = Logger.Null

    val engine = mock(classOf[Engine])
    when(engine.doUpdates()).thenThrow(new IllegalStateException("Some expected error"))

    intercept[IllegalStateException] {
      Update(engine)
    }

    verify(engine, atLeastOnce()).doUpdates()
  }

}
