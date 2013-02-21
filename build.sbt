scalaVersion in ThisBuild := "2.11.0-SNAPSHOT"

scalaOrganization := "org.scala-lang.macro-paradise"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

//scalacOptions ++= Seq("-Xlog-free-terms", "-explaintypes")

libraryDependencies in ThisBuild += "org.specs2" % "specs2_2.11.0-SNAPSHOT" % "1.13.1-SNAPSHOT" % "test"

libraryDependencies in ThisBuild += "org.specs2" % "specs2-scalaz-core_2.11.0-SNAPSHOT" % "1.13.1-SNAPSHOT" % "test"

libraryDependencies in ThisBuild += "org.specs2" % "specs2-scalaz-concurrent_2.11.0-SNAPSHOT" % "1.13.1-SNAPSHOT" % "test"

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies in ThisBuild <+= scalaVersion("org.scala-lang.macro-paradise" % "scala-reflect" % _)

resolvers in ThisBuild ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots")
