package cmu.s3d.fortis.web

import cmu.s3d.fortis.service.RobustificationService
import cmu.s3d.fortis.service.RobustnessComputationService
import cmu.s3d.fortis.service.WeakeningService
import cmu.s3d.fortis.service.impl.RobustificationServiceImpl
import cmu.s3d.fortis.service.impl.RobustnessComputationServiceImpl
import cmu.s3d.fortis.service.impl.WeakeningServiceImpl
import org.apache.logging.log4j.core.config.Configurator
import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject

fun main() {
    if (System.getProperty("log4j2.configurationFile") == null)
        Configurator.initialize(null, "log4j2-server.properties")

    val robustnessComputationService = UnicastRemoteObject.exportObject(
        RobustnessComputationServiceImpl(), 0) as RobustnessComputationService
    val robustificationService = UnicastRemoteObject.exportObject(
        RobustificationServiceImpl(), 0) as RobustificationService
    val weakeningService = UnicastRemoteObject.exportObject(
        WeakeningServiceImpl(), 0) as WeakeningService


    val registry = LocateRegistry.createRegistry(1099)
    registry.bind("RobustnessComputationService", robustnessComputationService)
    registry.bind("RobustificationService", robustificationService)
    registry.bind("WeakeningService", weakeningService)

    println("RMI Server started...")
}