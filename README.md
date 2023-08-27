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
| DENIED_WITH_RATIONALE | User has denied the permission but still has a chance to request for permission dialog |
| DENIED_WITH_NEVER_ASK | Not possible to request for permission dialog. (navigate user to App Settings) |

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

<br />

 * Use `modifier = Modifier.iconPulse()` for icon pulse effect 
  <p align="center">
   Alerter with icon, title and message
  </p>
 <p align="center">
 <img align="center" src="https://github.com/akardas16/Alerter/assets/28716129/7e036b7f-b024-44af-b8ac-0d5c3a8cd240" width="400">
</p>

 

```kotlin
 var showAlert by remember { mutableStateOf(false) }

 Alerter(isShown = showAlert, onChanged = {showAlert = it},
                backgroundColor = Color(0xFFF69346)) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {

                    Icon(imageVector = Icons.Rounded.Notifications, contentDescription = "",
                        tint = Color.White, modifier = Modifier.padding(start = 12.dp).iconPulse())

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(text = "Alert Title", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = "Alert text...", color = Color.White, fontSize = 14.sp)

                    }
                }
            }
```



<br />
<br />
