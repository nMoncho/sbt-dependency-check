package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckScanSet
import sbt.*

object ScanSet {

  def apply(): Def.Initialize[Task[Seq[File]]] = Def.task {
    dependencyCheckScanSet.value.map(_ ** "*").reduceLeft(_ +++ _).filter(_.isFile).get()
  }

}
