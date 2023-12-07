package cmu.s3d.fortis.web

import cmu.s3d.fortis.service.RobustificationService
import cmu.s3d.fortis.service.RobustnessComputationService
import cmu.s3d.fortis.service.impl.RobustificationServiceImpl
import cmu.s3d.fortis.service.impl.RobustnessComputationServiceImpl
import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject

fun main() {
    val robustnessComputationService = UnicastRemoteObject.exportObject(
        RobustnessComputationServiceImpl(), 0) as RobustnessComputationService
    val robustificationService = UnicastRemoteObject.exportObject(
        RobustificationServiceImpl(), 0) as RobustificationService


    val registry = LocateRegistry.createRegistry(1099)
    registry.bind("RobustnessComputationService", robustnessComputationService)
    registry.bind("RobustificationService", robustificationService)

    println("RMI Server started...")
}