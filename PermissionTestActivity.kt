class PermissionTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestProjectTheme {
                Greeting()

            }
        }
    }
}

@Composable
fun Greeting() {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var permissionStatus by remember {
        mutableStateOf(Status.INITIAL)
    }

    var showDeniedDialog by remember {
        mutableStateOf(false)
    }
    var showGrantedDialog by remember {
        mutableStateOf(false)
    }

    var showNavigateSettingDialog by remember {
        mutableStateOf(false)
    }




    var showRequestDialog by remember {
        mutableStateOf(false)
    }

    val request = requestPermission(permission = Manifest.permission.CAMERA,
        onChangedStatus = {
            if (((permissionStatus == Status.NOT_ASKED) or (permissionStatus == Status.DENIED_WITH_RATIONALE)) //User granted the permission
                and (it == Status.GRANTED_ALREADY)){
                showGrantedDialog = true
            }else if (((permissionStatus == Status.NOT_ASKED) and (it == Status.DENIED_WITH_RATIONALE)) //User denied the permission
                or ((permissionStatus == Status.DENIED_WITH_RATIONALE) and (it == Status.DENIED_WITH_NEVER_ASK))){
                showDeniedDialog = true
            }
            permissionStatus = it
        })



    Column(modifier = Modifier
        .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {


        SpecialDialog(showDialog = showRequestDialog, onChanged = {showRequestDialog = it}) {
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ), contentAlignment = Alignment.Center
            ){

                Column(modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                   Icon(imageVector = Icons.Rounded.PhotoCamera, contentDescription = "", tint = Color(0xFF1A97F7))


                    Text(text = "We need to access your camera to take photo", fontSize = 16.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .padding(horizontal = 12.dp), textAlign = TextAlign.Center)


                    Button(onClick = {
                        showRequestDialog = false
                        scope.launch {
                            delay(500)
                            request.launch(Manifest.permission.CAMERA)
                        }
                    }, modifier = Modifier
                        .width(180.dp)) {
                        Text(text = "Allow")
                    }

                    Button(onClick = {
                        showRequestDialog = false
                    }, modifier = Modifier
                        .width(180.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFFFF)
                    )) {
                        Text(text = "Close")
                    }



                }
            }
        }
        SpecialDialog(showDialog = showDeniedDialog, onChanged = {showDeniedDialog = it}) {
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ), contentAlignment = Alignment.Center
            ){

                Column(modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    Icon(imageVector = Icons.Rounded.PhotoCamera, contentDescription = "", tint = Color(0xFF1A97F7))


                    Text(text = "You denied Camera permission!", fontSize = 16.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .padding(horizontal = 12.dp), textAlign = TextAlign.Center)



                    Button(onClick = {
                        showDeniedDialog = false
                    }, modifier = Modifier
                        .width(180.dp)) {
                        Text(text = "Close")
                    }



                }
            }
        }
        SpecialDialog(showDialog = showGrantedDialog, onChanged = {showGrantedDialog = it}) {
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ), contentAlignment = Alignment.Center
            ){

                Column(modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    Icon(imageVector = Icons.Rounded.PhotoCamera, contentDescription = "", tint = Color(0xFF1A97F7))


                    Text(text = "You have Granted Camera permission!", fontSize = 16.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .padding(horizontal = 12.dp), textAlign = TextAlign.Center)



                    Button(onClick = {
                        showGrantedDialog = false
                    }, modifier = Modifier
                        .width(180.dp)) {
                        Text(text = "Close")
                    }



                }
            }
        }
        SpecialDialog(showDialog = showNavigateSettingDialog, onChanged = {showNavigateSettingDialog = it}) {
            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ), contentAlignment = Alignment.Center
            ){

                Column(modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    Icon(imageVector = Icons.Rounded.PhotoCamera, contentDescription = "", tint = Color(0xFF1A97F7))


                    Text(text = "You have disabled camera permission! You can navigate settings and enable Camera permission", fontSize = 16.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .padding(horizontal = 12.dp), textAlign = TextAlign.Center)



                    Button(onClick = {
                        showNavigateSettingDialog = false
                        scope.launch {
                            delay(500)
                            context.openAppSystemSettings()
                        }
                    }, modifier = Modifier
                        .width(180.dp)) {
                        Text(text = "Settings")
                    }



                }
            }
        }
        Button(onClick = {
            if (permissionStatus == Status.GRANTED_ALREADY){
                val intent = Intent("android.media.action.IMAGE_CAPTURE")
                context.findActivity().startActivityForResult(intent, 0)

            }else if ((permissionStatus == Status.NOT_ASKED) or (permissionStatus == Status.DENIED_WITH_RATIONALE)){
                showRequestDialog = showRequestDialog.not()
            }else if (permissionStatus == Status.DENIED_WITH_NEVER_ASK){
                request.launch(Manifest.permission.CAMERA)
                scope.launch {
                    delay(1000)
                    if (context.findActivity().hasWindowFocus()){ //See below for why hasWindowFocus should be true
                        showNavigateSettingDialog = true
                    }
                }
            }

        }) {
            Text(text = "Open Camera ${permissionStatus.name}")
        }

    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview7() {
    TestProjectTheme {
        Greeting()
    }
}

@Composable
fun SpecialDialog(showDialog:Boolean, onChanged:(isShown:Boolean) -> Unit, properties: DialogProperties = DialogProperties(),
                  content: @Composable () -> Unit){

    val animation by remember {
        mutableStateOf(androidx.compose.animation.core.Animatable(initialValue = 1f))
    }

    var show by remember {
        mutableStateOf(false)
    }


    LaunchedEffect(key1 = showDialog){

        if (showDialog) show = true
        launch {
            if (showDialog.not() and show) {
                delay(350)
                show = false
            }
        }
        launch {
            delay(75)
            animation.animateTo(targetValue = 1.05f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy,Spring.StiffnessMediumLow))
        }

        launch {
            delay(175)
            animation.animateTo(targetValue = 1f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy,Spring.StiffnessMediumLow))
        }


    }


    if (show){

        Dialog(onDismissRequest = {
            onChanged(false)

        }, properties = properties) {

            Box(modifier = Modifier.scale(animation.value)){
                content()
            }
        }
    }

}
