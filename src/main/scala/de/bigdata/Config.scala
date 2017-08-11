package de.bigdata

case class Config (
             maxIter: Int = 10,
             scoreFactor: Double = 0.5,
             topXCluster: Int = 3,
             clusterCapacity: Int = 2,
             input: String = ""
             )
