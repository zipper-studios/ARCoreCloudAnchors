package com.example.cloudanchorsardemo.fragment

import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

//This class extends ArFragment and create a configuration for ArFragment
class ArCoreFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {
//        Set the instruction view of planeDiscoveryController to null as a step to disable the initial hand gesture.
        planeDiscoveryController.setInstructionView(null)

//        Create an ArCoer configuration
        val config = super.getSessionConfiguration(session)

//        Enable Cloud Anchor mode
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED

//        Return ArFragment configuration
        return config

    }
}