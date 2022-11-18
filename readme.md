# Download a file

This app is demonstrating 4 different methods to download a file from an URL (e.g. internet) to the 
local external storage (here download folder).

The app is running a runtime permission check and please grant the requested permission, otherwise the app will stop. 

Th get the filename and filepath of the file to store I'm using the Android's system file picker with an intent ( 
except for the DownloadManager example)

This are the 4 methods:

* using the DownloadManager (you know them from your device, the actual download status is shown in the statusbar)
* using the OkHttps library
* using a stream (note: my implementation is running completely on the MainThread and your device will get blocked 
completely until the download is finished)
* using a HttpUrlConnection

My favourite method is HttpUrlConnection as it provides to monitor the download progression.
