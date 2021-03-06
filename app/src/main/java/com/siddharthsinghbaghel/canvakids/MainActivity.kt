package com.siddharthsinghbaghel.canvakids

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint: ImageButton? = null
    private var mPickerDefaultColor = Color.MAGENTA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "🎨  Canva Kids"
        setContentView(R.layout.activity_main)


        drawing_view.setSizeForBrush(20.toFloat())

        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton  //ll_paint_colors is the name of linear layout and we can access elements of linear layout according to the position they are placed im it by using [].
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )

        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
        ib_gallery.setOnClickListener {
            if(isReadStorageAllowed()){
                   /* Show gallery code */
                val pickPhotoIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

             startActivityForResult(pickPhotoIntent, GALLERY)
            }
            else{
                 requestStoragePermission()
            }
        }
        ib_undo.setOnClickListener {
            drawing_view.onClickUndo()
        }
        ib_Redo.setOnClickListener {

            drawing_view.onClickRedo()
        }
        ib_save.setOnClickListener {

            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitMapFromView(fl_drawing_view_container)).execute()
            }else{
                requestStoragePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK)
        {
            if(requestCode == GALLERY)
            {
                try{
                    if(data!!.data != null){
                            iv_background.visibility = View.VISIBLE
                            iv_background.setImageURI(data.data)
                    }else{
                        Toast.makeText(
                            this,
                            "Error in image parsing or image is corrupted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }catch (e: Exception){
                    Toast.makeText(this, "${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
                }
            }

            if(requestCode == IMAGE_SAVED_CODE)
            {
                try{
                    if(data!!.data != null){
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data.data)
                    }else{
                        Toast.makeText(
                            this,
                            "Error in image parsing or image is corrupted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }catch (e: Exception){
                    Toast.makeText(this, "${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
                }
            }
            if(requestCode == CAMERA_ACCESS_CODE)
            {

                try{
                      val bitmap = data?.extras?.get("data") as Bitmap
                       iv_background.setImageBitmap(bitmap)
                    }
                catch (e: Exception){
                    Toast.makeText(this, "${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size: ")
        val smallButton = brushDialog.ib_small_brush
        smallButton.setOnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumButton = brushDialog.ib_medium_brush
        mediumButton.setOnClickListener {
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        val largeButton = brushDialog.ib_large_brush
        largeButton.setOnClickListener {
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()

    }

    fun paintClicked(view: View){
        if(view != mImageButtonCurrentPaint)
        {
            val imageButton = view as ImageButton

            val colorTag = imageButton.tag.toString()
            drawing_view.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint =view
        }

    }

    private fun requestStoragePermission(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )) {


            Toast.makeText(this, "Need permission to add Background", Toast.LENGTH_SHORT).show()

        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), STORAGE_PERMISSION_CODE
        )
    }
    private fun requestCameraPermission(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this, arrayOf(
                    Manifest.permission.CAMERA
                ).toString()
            )) {

                Toast.makeText(this, "Need permission to add Background", Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CAMERA,
            ), CAMERA_ACCESS_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Storage Permission Granted ", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }
        if(requestCode == CAMERA_ACCESS_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Camera Permission Granted ", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun isReadStorageAllowed() :Boolean{
         val result = ContextCompat.checkSelfPermission(
             this,
             Manifest.permission.READ_EXTERNAL_STORAGE
         )
         return result == PackageManager.PERMISSION_GRANTED
    }
    private fun isCameraAllowed() :Boolean{
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitMapFromView(view: View): Bitmap {

        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null)
        {
            bgDrawable.draw(canvas)
        }
        else{
            canvas.drawColor(Color.rgb(0, 184, 212))
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private inner class BitmapAsyncTask(val mBitmap: Bitmap):
                        AsyncTask<Any, Void, String>(){

        override fun doInBackground(vararg params: Any?): String {

               var result: String = ""

               if(mBitmap != null)
               {
                   try{

                       val title : String = "KidsDrawingApp"+ System.currentTimeMillis() / 1000 + ".png"
                       val bytesOutput = ByteArrayOutputStream()
                       mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytesOutput)
                       val f = File(
                           externalCacheDir!!.absoluteFile.toString()
                                   + File.separator + "KidsDrawingApp"
                                   + System.currentTimeMillis() / 1000
                                   + ".png"
                       )
                       val  g = MediaStore.Images.Media.insertImage(
                           contentResolver,
                           mBitmap,
                           title,
                           "Image of $title"
                       )

                       val fileOutpotStream = FileOutputStream(f)
                       fileOutpotStream.write(bytesOutput.toByteArray())
                       fileOutpotStream.close()
                       result = f.absolutePath

                   }catch (e: Exception){
                            result = ""
                            e.printStackTrace()
                   }
               }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

           if(!result?.isEmpty()!!){
               val snackbar= Snackbar.make(
                   drawing_view, "🏳 Your file is safe now in gallery of phone! ",
                   Snackbar.LENGTH_LONG
               ).setAction("Action", null)
               snackbar.setActionTextColor(Color.BLUE)
               snackbar.show()
           }else{
               val snackbar= Snackbar.make(
                   drawing_view, "🥵 Something went wrong, file not saved! ",
                   Snackbar.LENGTH_LONG
               ).setAction("Action", null)
               snackbar.setActionTextColor(Color.BLUE)
               snackbar.show()
           }



      /*   MediaScannerConnection.scanFile(
                this@MainActivity,
                arrayOf(result),
                null
            ){ _, uri->
             val shareIntent = Intent()
             shareIntent.action = Intent.ACTION_SEND
             shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
             shareIntent.type = "image/png"
             startActivity(Intent.createChooser(shareIntent, "Share with friends"))

         }*/

        }



    }

    companion object{
        private const val STORAGE_PERMISSION_CODE =1
        private const val GALLERY = 2
        private const val IMAGE_SAVED_CODE = 3
        private const val CAMERA_ACCESS_CODE = 4
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_saved_item -> {

                Toast.makeText(this, "Saved Images ", Toast.LENGTH_SHORT).show()

                if (isReadStorageAllowed()) {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.setDataAndType(
                        Uri.parse(
                            Environment.getExternalStorageDirectory().path
                                .toString() + File.separator + "" + File.separator
                        ), "image/png"
                    )
                    startActivityForResult(intent, IMAGE_SAVED_CODE)
                } else {
                    requestStoragePermission()
                }
            }

            R.id.menu_get_camera -> {
                Toast.makeText(this, "Get images ", Toast.LENGTH_SHORT).show()

                if (isCameraAllowed()) {

                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                        intent.resolveActivity(packageManager)?.also {
                            startActivityForResult(intent, CAMERA_ACCESS_CODE)
                        }
                    }
                } else {
                    requestCameraPermission()
                }
            }
            R.id.menu_info -> {
                Toast.makeText(this, "Info ", Toast.LENGTH_SHORT).show()
                val infoIntent = Intent(this, InfoActivity::class.java)
                startActivity(infoIntent)
            }
            R.id.imgPicker -> {
                openColorPicker();
            }
            R.id.save -> {

                if (isReadStorageAllowed()) {
                    BitmapAsyncTask(getBitMapFromView(fl_drawing_view_container)).execute()

                } else {
                    requestStoragePermission()
                }

            }

        }

        return super.onOptionsItemSelected(item)
    }

    fun openColorPicker() {
        val colorPicker = AmbilWarnaDialog(
            this,
            mPickerDefaultColor,
            object : OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {}
                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {

                    mPickerDefaultColor = color
                    drawing_view.setPickerColor(mPickerDefaultColor)
                }
            })
        colorPicker.show()
    }
}