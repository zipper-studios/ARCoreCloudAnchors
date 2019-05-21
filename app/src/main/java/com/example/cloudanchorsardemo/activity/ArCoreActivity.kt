package com.example.cloudanchorsardemo.activity

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.cloudanchorsardemo.R
import com.example.cloudanchorsardemo.database.FirebaseDatabaseManager
import com.example.cloudanchorsardemo.dialog.ResolveDialog
import com.example.cloudanchorsardemo.fragment.ArCoreFragment
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_arcore.*


class ArCoreActivity : AppCompatActivity() {

    //    Declare a CloudAnchor and an AppAnchorState
    private var cloudAnchor: Anchor? = null
    private var appAnchorState = AppAnchorState.NONE

    private var arCoreFragment: ArCoreFragment? = null
    private var firebaseDatabaseManager: FirebaseDatabaseManager? = null

    //    The AppAnchorState will contain the status of the cloudAnchor
    private enum class AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcore)

        arCoreFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArCoreFragment?
        arCoreFragment?.planeDiscoveryController?.hide()
        arCoreFragment?.arSceneView?.scene?.addOnUpdateListener {
            Scene.OnUpdateListener { p0 -> updateAnchorIfNecessary() }
        }
        arCoreFragment?.arSceneView?.scene?.addOnUpdateListener { p0 -> updateAnchorIfNecessary() }
        firebaseDatabaseManager = FirebaseDatabaseManager(this)

        initListeners()
    }

    //    This method is responsible for setting listeners on clear and resolve buttons.
//    It also sets the TapArPlaneListener on ArCoreFragment and hides plane discovery controller to disable the hand gesture (optional)
    private fun initListeners() {
        clear_button.setOnClickListener {
            //            Set Cloud Anchor to null
            setCloudAnchor(null)
        }

        resolve_button.setOnClickListener(View.OnClickListener {
            ResolveDialog(
                this,
                object : ResolveDialog.PositiveButtonListener {
                    override fun onPositiveButtonClicked(dialogValue: String) {
                        resolveAnchor(dialogValue)
                    }
                },
                getString(R.string.resolve),
                View.VISIBLE,
                View.VISIBLE
            ).show()
        })


//      When an upward facing plane is tapped, we create an Anchor and pass it to setCloudAnchor function
        arCoreFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->

            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING || appAnchorState != AppAnchorState.NONE) {
            }

            val newAnchor = arCoreFragment?.arSceneView?.session?.hostCloudAnchor(hitResult.createAnchor())
            setCloudAnchor(newAnchor)
            appAnchorState = AppAnchorState.HOSTING
            Toast.makeText(this, "Now hosting anchor...", Toast.LENGTH_LONG).show()

//          call placeObject() on the cloudAnchor, placing our 3D wolf at the tapped point.
            arCoreFragment?.let { placeWolf(it, cloudAnchor, Uri.parse("Wolf_One_obj.sfb")) }
        }
    }

    fun resolveAnchor(dialogValue: String) {
        val shortCode = Integer.parseInt(dialogValue)
        firebaseDatabaseManager?.getCloudAnchorID(shortCode, object :
            FirebaseDatabaseManager.CloudAnchorIdListener {
            override fun onCloudAnchorIdAvailable(cloudAnchorId: String?) {
                val resolvedAnchor = arCoreFragment?.arSceneView?.session?.resolveCloudAnchor(cloudAnchorId)
                setCloudAnchor(resolvedAnchor)
                showMessage("Now Resolving Anchor...")
                arCoreFragment?.let { placeWolf(it, cloudAnchor, Uri.parse("Wolf_One_obj.sfb")) }
                appAnchorState = AppAnchorState.RESOLVING
            }

        })
    }

    //    This method shows some messages to the users using Toast
    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    //    This method sets a new Cloud Anchor and ensures there is only one cloud anchor at any point of time
    private fun setCloudAnchor(newAnchor: Anchor?) {
        if (cloudAnchor != null) {
            cloudAnchor?.detach()
        }

        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
    }

    //    This method checks the anchor state and update it if necessary
    @Synchronized
    private fun updateAnchorIfNecessary() {
        if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING) {
            return
        }
        val cloudState = cloudAnchor?.cloudAnchorState
        cloudState?.let { it ->
            if (appAnchorState == AppAnchorState.HOSTING) {
                if (it.isError) {
                    Toast.makeText(this, "Error hosting anchor.. $it", Toast.LENGTH_LONG).show()

                    appAnchorState = AppAnchorState.NONE
                } else if (it == Anchor.CloudAnchorState.SUCCESS) {
                    firebaseDatabaseManager?.nextShortCode(object :
                        FirebaseDatabaseManager.ShortCodeListener {
                        override fun onShortCodeAvailable(shortCode: Int?) {
                            if (shortCode == null) {
                                showMessage("Could not get shortCode")
                                return
                            }
                            cloudAnchor?.let {
                                firebaseDatabaseManager?.storeUsingShortCode(
                                    shortCode,
                                    it.cloudAnchorId
                                )
                            }
                            showMessageWitAnchorId("Anchor hosted! Cloud Short Code: $shortCode")
                        }

                    })
                    appAnchorState = AppAnchorState.HOSTED
                }
            } else if (appAnchorState == AppAnchorState.RESOLVING) {
                if (it.isError) {
                    Toast.makeText(this, "Error hosting anchor.. $it", Toast.LENGTH_LONG).show()

                    appAnchorState = AppAnchorState.NONE
                } else if (it == Anchor.CloudAnchorState.SUCCESS) {
                    Toast.makeText(this, "Anchor resolved successfully", Toast.LENGTH_LONG).show()

                    appAnchorState = AppAnchorState.RESOLVED
                }
            }
        }

    }

    private fun showMessageWitAnchorId(s: String) {
        ResolveDialog(
            this,
            object : ResolveDialog.PositiveButtonListener {
                override fun onPositiveButtonClicked(dialogValue: String) {
                    resolveAnchor(dialogValue)
                }
            },
            s,
            View.GONE,
            View.GONE
        ).show()
    }

    //      This method builds the model using ModelRenderable class.
//      It creates the ModelRenderable by setting the source that will load the model.
//        - thenAccept() is a method that will be called if the model will be successfully loaded and will return the built model.
//        -  exceptionally() method will be called if the model will not be created.
    private fun placeWolf(fragment: ArFragment, anchor: Anchor?, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable) }
            .exceptionally { throwable ->
                val builder = android.app.AlertDialog.Builder(this)
                builder.setMessage(throwable.message)
                    .setTitle("Error!")
                val dialog = builder.create()
                dialog.show()
                null
            }

    }

    //    This function is responsible for adding the node to scene.
//    It creates an AnchorNode on the Anchor and a TransformableNode with the parent as AnchorNode.
//    - An Anchor describes a fixed location and orientation in the real world
//    - An AnchorNode is the first node that gets set when the plane is detected
//    - The Scene is the space where 3D object will be placed.
//    - TransformableNode is a node that can be interacted with. It can be moved around, scaled, rotated and much more.
//    - HitResult can be seen as an infinite imaginary line that gives the point of intersection of itself and the real world.
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor?, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }
}
