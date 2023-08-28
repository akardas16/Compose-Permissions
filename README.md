# Compose-Permissions
* A library to handle permissions in Jetpack Compose


## Install

* Add [`ComposePermission.kt`](https://github.com/akardas16/Compose-Permissions/blob/main/ComposePermission.kt) file to your project 

# Usage

<br />

| Status | Description |
| --- | --- |
| GRANTED_ALREADY | User has already granted permission |
| NOT_ASKED | User has never requested the permission (Possible to show permission dialog) |
| DENIED_WITH_RATIONALE | User has denied the permission but still has a chance to request for permission dialog (Possible to show permission dialog) |
| DENIED_WITH_NEVER_ASK | Not possible to show permission dialog. (navigate user to App Settings) |

<br />

## Single Permission Request

* Track status of permission with `permissionStatus`
```kotlin
 var permissionStatus by remember { // 
        mutableStateOf(Status.INITIAL)
    }
 val request = requestPermission(permission = android.Manifest.permission.CAMERA,
        onChangedStatus = { permissionStatus = it}) 
```
<br />

* To request permission 


```kotlin
 request.launch(android.Manifest.permission.CAMERA)
``` 
<br />
<br />

* See complete example below for single permission request

```kotlin
@Composable
fun Greeting() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permissionStatus by remember { // Track status of permission
        mutableStateOf(Status.INITIAL)
    }

    val request = requestPermission(permission = android.Manifest.permission.CAMERA,
        onChangedStatus = { permissionStatus = it}) 

    Column(modifier = Modifier
        .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        //you can use if(permissionStatus) if you don't interest all states 
        when(permissionStatus){
            Status.GRANTED_ALREADY -> {//(No need to request permission) or (permission requested and granted already)
                Text(text = "Permission Has already Granted")
            }
            Status.NOT_ASKED -> {//a place to request permission
                Text(text = "No permission request has made")
            }
            Status.DENIED_WITH_RATIONALE -> {
                Text(text = "Permission has denied once but you have still have a chance to show permission popup")
            }
            Status.DENIED_WITH_NEVER_ASK -> {//call context.openAppSystemSettings() to navigate user to app settings

                Text(text = "Permission has denied navigate user to app settings")
            }
            else -> {}//Nothing
        }

        Button(onClick = {
            //Request permission
            request.launch(android.Manifest.permission.CAMERA)
            scope.launch {
                delay(1000)
                if (permissionStatus == Status.DENIED_WITH_NEVER_ASK
                    && context.activity()?.hasWindowFocus() == true){ //See below for why hasWindowFocus should be true
                    context.openAppSystemSettings()
                }
            }

        }) { Text("Request permission") }

    }
}
```
## Multiple Permissions Request

* Track status of permissions with `permissionStatus`
```kotlin
var permissionStatus by remember {
        mutableStateOf(mapOf<String,Status>())
    }
val request = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS
        ), onChangedStatus = { permissionStatus = it}
    )
```
<br />

* To request multiple permissions

```kotlin
 request.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS))
``` 
<br />
<br />

* See complete example below for multiple permissions request

```kotlin
 @Composable
fun Greeting() {

    var permissionStatus by remember {
        mutableStateOf(mapOf<String,Status>())
    }

    val request = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS
        ), onChangedStatus = { permissionStatus = it}
    )

    Column(modifier = Modifier
        .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        if (permissionStatus.allGranted()){
            Text(text = "All Permissions have already Granted")
        }

        if (permissionStatus.allDenied()){
            Text(text = "All Permissions have not Granted")
        }
        Row {
            Text(text = permissionStatus.keys.first().filter { it.isUpperCase() }, fontSize = 12.sp)
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "")
            Text(text = permissionStatus.values.first().name, fontSize = 12.sp)
        }

        Row {
            Text(text = permissionStatus.keys.last().filter { it.isUpperCase() }, fontSize = 12.sp)
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "")
            Text(text = permissionStatus.values.last().name, fontSize = 12.sp)
        }

        Button(onClick = {
            //Request permission
            request.launch(arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS))

        }) { Text("Request permissions") }

    }
}
```
<br />
<br />

* If all permissions have granted, `permissionStatus.allGranted()` will be true 
* If all permissions have not granted, `permissionStatus.allDenied()` will be true

```kotlin
 fun Map<String,Status>.allGranted():Boolean{
   return this.values.all { it == Status.GRANTED_ALREADY }
}
fun Map<String,Status>.allDenied():Boolean{
    return this.values.all { it != Status.GRANTED_ALREADY }
}
```
<br />
<br />

* It is not possible to observe changes, if user has manually changed permission in app settings
* If user has denied permission with never_ask and changed permission manually, it will be better to request permission and check any window popuped or not. 
  
```kotlin
 request.launch(android.Manifest.permission.CAMERA)
            scope.launch {
                delay(1000)
                if (permissionStatus == Status.DENIED_WITH_NEVER_ASK
                    && context.activity()?.hasWindowFocus() == true){ 
                    context.openAppSystemSettings()
                }
            }
```


