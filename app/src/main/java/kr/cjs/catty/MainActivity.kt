package kr.cjs.catty

import android.os.Build
import android.Manifest
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.*
import kotlinx.coroutines.launch
import kr.cjs.catty.ui.theme.CattyTheme
import kr.cjs.catty.view.Department
import kr.cjs.catty.view.Product
import kr.cjs.catty.viewmodel.*
import kr.cjs.catty.viewmodel.SuppliesViewModel.Companion.productId
import kr.cjs.catty.viewmodel.SuppliesViewModel.Companion.productName
import kr.cjs.catty.viewmodel.SuppliesViewModel.Companion.quantity
import kr.cjs.catty.viewmodel.SuppliesViewModel.Companion.selectedDepartment
import kr.cjs.catty.viewmodel.SuppliesViewModel.Companion.updateState
import java.net.URLDecoder
import java.net.URLEncoder
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import kr.cjs.catty.view.decodeHtml
import dagger.hilt.android.AndroidEntryPoint
import androidx.annotation.RequiresApi

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import androidx.compose.ui.res.colorResource

import android.os.Bundle

import kr.cjs.catty.view.*






@AndroidEntryPoint
class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        KeyHashUtil.getHashKey(this)



        setContent {
            ScreenTabCarousel()
        }
    }
}

enum class Screen(
    val title: String
) {
    Home(title = "Home"),
    NaverSearchDetail(title = "Webview"),
    Third(title = "Stateful Counter"),
    Fourth(title = "Lotto Simulator"),
    Fifth(title = "Song List"),
    Sixth(title = "Android Intents"),
    Seventh(title = "Custom View"),
    NaverSearch(title = "Android(Retrofit)"),
    Ninth(title = "Android(Room)"),
    Tenth(title = "Resource Tenth"),
    Eleventh(title="Swipe Screen"),

    KakaoMap(title = "KAKAO Map Search"),

}

@RequiresApi(Build.VERSION_CODES.M)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenTabCarousel(
    pages: MutableList<String> = arrayListOf(
            "미니프로젝트",
            "카카오맵",
            "Mediapipe1",
            "Mediapipe2",
            "물품관리"
            )
){
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Column {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            containerColor =  Color(context.resources.getColor(R.color.white, null)),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                )
            }
        ) {
            pages.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index

                TabHeader(
                    title,
                    isSelected,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
        }

        val pageScreens: List<@Composable () -> Unit> = listOf(
            { MainScreen() },
            { KakaoMapScreen() },
            {ObjectDetectionScreen()},
            {CameraDetectionScreen()},
            { SixthScreen(modifier = Modifier.fillMaxSize()) },
            { SwipeScreen(modifier = Modifier.fillMaxSize()) }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            pageScreens[page].invoke()
        }
    }

}

@RequiresApi(Build.VERSION_CODES.M)
@Composable
private fun TabHeader(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val color = if (isSelected) R.color.purple_700 else R.color.white
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .selectable(
                selected = isSelected,
                onClick = { onClick() },
                enabled = true,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = ripple()
            )
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        TabCarousel(title = title, isSelected = isSelected)
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@Composable
private fun TabCarousel(title: String, isSelected: Boolean) {
    val context = LocalContext.current
    val backgroundColor = if (isSelected) colorResource(id = R.color.teal_200) else colorResource(id = R.color.white)
    val textColor = if (isSelected) colorResource(id = R.color.purple_700) else colorResource(id = R.color.black)
    Row(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
            .width(if (title.length < 11) 70.dp else 110.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(
            text = title,
            color = textColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    CattyTheme  {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ModalDrawerSheet {
                    Box {

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row (
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween

                            ){
                                Text(
                                    "Menu",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF9c5867)
                                )
                                IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = Color(0xFF9c5867))
                                }
                            }
                            HorizontalDivider(color = Color.Gray)

                            Spacer(modifier = Modifier.height(16.dp))
                            val mainScreens = listOf(Screen.NaverSearch,Screen.Ninth,Screen.KakaoMap )

                            mainScreens.forEach { screen ->
                                NavigationDrawerItem(
                                    label = { Text(text = screen.title, color =Color(0xFF9c5867)) },
                                    selected = false,
                                    onClick = {
                                        navController.navigate(screen.name)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }

                        }
                    }
                }
            }
        ){            
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { TopAppBar(onMenuClick = { scope.launch { drawerState.open() } }) },
                bottomBar = { AppBottomBar(onDisplayClick = { showBottomSheet = true }) }
            ) { innerPadding ->
                val modifier = Modifier.padding(innerPadding)


                NavHost(navController = navController, startDestination = Screen.Home.name, modifier = modifier) {
                    composable(Screen.Home.name) {
                        HomeScreen(
                            modifier = Modifier.fillMaxSize(),
                            onNavigateToSimulation = { navController.navigate(Screen.Fourth.name) },
                            onNavigateToSecond = { navController.navigate("${Screen.NaverSearchDetail.name}/$it") }
                        )
                    }
                    composable( route = "${Screen.NaverSearchDetail.name}/{arg}" ) { backStackEntry ->
                        val encodeUrl = backStackEntry.arguments?.getString("arg") ?: ""
                        val url = URLDecoder.decode(encodeUrl,"UTF-8")
                        NaverSearchDetailScreen(url)
                    }
                    composable(Screen.Third.name) { ThirdScreen(modifier = Modifier.fillMaxSize()) }
                    composable(Screen.Fourth.name) { FourthScreen(modifier = Modifier.fillMaxSize()) }
                    composable(Screen.Fifth.name){ FifthScreen(modifier = Modifier.fillMaxSize())}
                    composable(Screen.Sixth.name){ SixthScreen(modifier = Modifier.fillMaxSize())}
                    composable(Screen.Seventh.name){ SeventhScreen(modifier = Modifier.fillMaxSize())}
                    composable(Screen.NaverSearch.name){ NaverSearchScreen(modifier = Modifier.fillMaxSize(), navController = navController)}
                    composable(Screen.Ninth.name){ InventoryUpdateScreen(modifier = Modifier.fillMaxSize()) }
                    composable(Screen.Tenth.name){ RequirementScreen(modifier = Modifier.fillMaxSize()) }
                    composable(Screen.Eleventh.name){ SwipeScreen(modifier = Modifier.fillMaxSize()) }
                    composable(Screen.KakaoMap.name){ MapPermission(modifier = Modifier.fillMaxSize()) }

                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Bottom Sheet Content")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(onMenuClick: () -> Unit){
    TopAppBar(
        title = { Text("Spoil Your Feline Friend 🐱🐱‍🐱‍")},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFE3ED),
            titleContentColor = Color(0xFF9c5867),
            navigationIconContentColor = Color(0xFF9c5867),
            actionIconContentColor = Color(0xFF9c5867)
        ),
        navigationIcon = {
            IconButton(onClick = onMenuClick){
                Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
            }
        },
    )

}
    
@Composable
fun AppBottomBar(onDisplayClick: () -> Unit) {

    BottomAppBar(
        modifier = Modifier.height(64.dp),
        containerColor = Color(0xFFFFE3ED),
        contentColor = Color(0xFF9c5867),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onDisplayClick) {
                Image(
                    painter = painterResource(id = R.drawable.catfoot),
                    contentDescription = "Display Bottom Sheet",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier,
    onNavigateToSimulation: () -> Unit,
    onNavigateToSecond: (String) -> Unit
){

    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(if (SDK_INT >= 28) ImageDecoderDecoder.Factory() else GifDecoder.Factory())
            }
            .build()
    }

    Column(
        modifier = Modifier.background(Color(0xFFFFE3ED))
    ) {
        AsyncImage(
                model = R.drawable.catkiss,
                contentDescription = "Animated Cat Kissing",
                imageLoader = imageLoader,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(horizontal = 30.dp)
        )

        SwipeScreen(modifier = Modifier
            .weight(1.5f)
            .fillMaxSize())

    }
}

@Composable
fun SimulationBtn(
    onSimulateClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Button(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onSimulateClick,
        colors = ButtonDefaults.buttonColors(Color.Black)
    ){
        Text("click list", fontSize = 20.sp,color = Color.Red)
    }
}

@Composable
fun NaverSearchDetailScreen(arg:String){

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadUrl(arg)
            }
        },

        update = { webView ->
            webView.loadUrl(arg)
        }
    )
}

@Composable
fun ThirdScreen(modifier: Modifier) {
    var count1 by rememberSaveable { mutableIntStateOf(0) }
    var count2 by rememberSaveable { mutableIntStateOf(0) }

        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(modifier = modifier) {
                Box(Modifier.weight(1F)){
                    Counter(
                        Modifier.background(Color(0xFFD0CCFF)),
                        count = count1,
                        onCountChanged = { count1 = it })
                }
                Box(Modifier.weight(1F)) {
                    Counter(
                        Modifier.background(Color(0xFFD0BCFF)),
                        count = count2,
                        onCountChanged = { count2 = it })
                }
            }

        } else {
                Row {
                    Box(Modifier.weight(1F)) {
                        Counter(
                            Modifier.background(Color(0xFFD0CCFF)),
                            count = count1,
                            onCountChanged = { count1 = it })
                    }
                    Box(Modifier.weight(1F)) {
                        Counter(
                            Modifier.background(Color(0xFFD0BCFF)),
                            count = count2,
                            onCountChanged = { count2 = it })
                    }
                }
        }
}
@Composable
fun Counter(modifier: Modifier, count: Int,onCountChanged:(Int)->Unit){

    Column(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxSize()
            .background(Color(0xFFEADDFF)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = count.toString(),
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            fontSize = 100.sp,
            textAlign = TextAlign.Center,

        )
        Row {
            Button(
                modifier = Modifier.padding(8.dp),
                onClick = { onCountChanged(count  + 1)}
            ){
                Text("증가", fontSize = 30.sp)
            }
            Button(
                modifier = Modifier.padding(8.dp),
                onClick = { if (count>0) onCountChanged( count -1)}
            ){
                Text("감소", fontSize = 30.sp)
            }
        }
    }
}

@Composable
fun FourthScreen(modifier: Modifier){

    val viewModel: LottoViewModel = viewModel()
    Column(modifier = modifier) {
        Button(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            onClick = { viewModel.generate() }
        ) {
            Text(text = "번호 생성", fontSize = 20.sp)
        }

        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LandScapeScreen(viewModel)
        } else {
            PortraitScreen(viewModel)
        }
    }
}

@Composable
fun LandScapeScreen(viewModel: LottoViewModel) {
    Row(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(Color(0xFFFEF7FF)),
    ) {
        viewModel.numbers.map {
            Text(
                it.toString(),
                Modifier
                    .weight(1F)
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color(0xFF845EC2))
                    .wrapContentSize(),
                fontSize = 50.sp,
                color = Color(0xFFFEFEDF),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun RowScope.TextNumber(number: Int) {
    Text(
        text = number.toString(),
        modifier = Modifier
            .weight(1F)
            .fillMaxSize()
            .background(Color(0xFF845EC2))
            .padding(8.dp)
            .wrapContentSize(),
        fontSize = 50.sp,
        color = Color(0xFFFEFEDF),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun PortraitScreen(viewModel: LottoViewModel) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color(0xFFFEF7FF)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        viewModel.numbers.chunked(2).forEach { rowNumbers ->
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowNumbers.forEach { number ->
                    TextNumber(number)
                }

                if (rowNumbers.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
fun Counter2(
    modifier: Modifier = Modifier,
    viewModel: CounterViewModel = viewModel(),
){

    val count by viewModel.count1

    Column(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .background(Color(0xFFEADDFF)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(

            text = count.toString(),
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            fontSize = 100.sp,
            textAlign = TextAlign.Center,

            )
        Row {
            Button(
                modifier = Modifier.padding(8.dp),

                onClick = { viewModel.incrementCount1() }
            ){
                Text("증가", fontSize = 30.sp)
            }
            Button(
                modifier = Modifier.padding(8.dp),
                onClick = {
                    if (count > 0) viewModel.decrementCount1()
                }
            ){
                Text("감소", fontSize = 30.sp)
            }
        }
    }
}

@Composable
fun FifthScreen(modifier: Modifier){
    val viewModel: SongViewModel = viewModel()

    LaunchedEffect(Unit) {
        if (viewModel.songs.isEmpty()) {

            viewModel.add(Song("리스트 제목 1", "임영웅"))
            viewModel.add(Song("리스트 제목 2", "임창정"))
            viewModel.add(Song("리스트 제목 3", "왁스"))
            viewModel.add(Song("리스트 제목 4", "박상민"))
            viewModel.add(Song("리스트 제목 5", "임영웅"))
            viewModel.add(Song("리스트 제목 6", "임창정"))
            viewModel.add(Song("리스트 제목 7", "왁스"))
            viewModel.add(Song("리스트 제목 8", "박상민"))
        }
    }

    LazyColumn(
        // 5. 스크롤이 가능하도록 LazyColumn에 modifier를 적용합니다.
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(viewModel.songs) { song ->
            SongItem(song)
        }
    }

}

@Composable
fun SongItem(song: Song) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Row(
            Modifier.height(IntrinsicSize.Max)
        ) {
            Box(
                Modifier
                    .padding(8.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img1),
                    contentDescription = "Album art for ${song.title}",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                SongTitle(song.title)
                SingerName(song.singer)
            }
        }
    }
}

@Composable
fun SongTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 25.sp,
        lineHeight = 30.sp,
    )
}

@Composable
fun SingerName(name: String) {
    Text(name, fontSize = 20.sp)
}

@Composable
fun SixthScreen(modifier: Modifier){
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ButtonHomepage()
        ButtonYoutube()
        ButtonCoordinates()
        ButtonMap()
        ButtonCall()
        ButtonSms()
        ButtonKakao()

    }

}

@Composable
fun ButtonHomepage() {
    val context = LocalContext.current

    Button(
        onClick = {

            val uri = Uri.parse("https://github.com/cjs2016")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(context.packageManager) == null) {
                Toast.makeText(
                    context,
                    "처리할 앱이 없습니다.",
                    Toast.LENGTH_LONG).show()
                return@Button
            }
            startActivity(context, intent, null)
        }
    ) {
        Text("홈페이지", fontSize = 20.sp)
    }
}

@Composable
fun ButtonYoutube() {
    val context = LocalContext.current

    Button(
        onClick = {
            val uri = Uri.parse("https://www.youtube.com/playlist?list=PLWz5rJ2EKKc94tpHND8pW8Qt8ZfT1a4cq")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(context, intent, null)
        }
    ) {
        Text("유튜브", fontSize = 20.sp)
    }
}

@Composable
fun ButtonCoordinates() {
    val context = LocalContext.current

    Button(
        onClick = {
            val uri = Uri.parse("geo:36.145014,128.393047?z=17")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(context, intent, null)
        }
    ) {
        Text("경위도 좌표", fontSize = 20.sp)
    }
}

@Composable
fun ButtonMap() {
    val context = LocalContext.current

    Button(
        onClick = {

            val uri = Uri.parse("geo:0,0?q=경북 구미시 대학로 61")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(context, intent, null)
        }
    ) {
        Text("지도 검색", fontSize = 20.sp)
    }
}

@Composable
fun ButtonCall() {
    val context = LocalContext.current

    Button(
        onClick = {
            val uri = Uri.parse("tel:010-1234-5678")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(context, intent, null)
        }
    ) {
        Text("전화 걸기", fontSize = 20.sp)
    }
}

@Composable
fun ButtonSms() {
    val context = LocalContext.current

    Button(
        onClick = {

            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("sms:010-1234-5678")
                putExtra("sms_body", "이용해 주셔서 감사합니다.")
            }
            startActivity(context, intent, null)
        }
    ) {
        Text("문자 보내기", fontSize = 20.sp)
    }
}


@Composable
fun ButtonKakao() {
    //https://developers.kakao.com/
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setPackage("com.kakao.talk")
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, "이용해 주셔서 감사합니다.")
            try {
                startActivity(context, intent, null)
            } catch (_: ActivityNotFoundException) {
                val kakaoApp = Uri.parse("market://details?id=com.kakao.talk") // 6. 오타 수정
                val market = Intent(Intent.ACTION_VIEW, kakaoApp)

                // NOTE: PlayStore 설치되어 있지 않으면 역시 에러 발생
                if (market.resolveActivity(context.packageManager) == null) {
                    Toast.makeText(
                        context,
                        "처리할 앱과 플레이스토어가 없습니다.",
                        Toast.LENGTH_LONG).show()
                    return@Button
                }
                startActivity(context, market, null)
            }
        }
    ) {
        Text("카카오톡", fontSize = 20.sp)
    }
}

@Composable
fun SeventhScreen(modifier: Modifier){

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { content ->
                MyCanvasView(content)
            }
        )
    }
}

@Composable
fun InventoryScreen(modifier: Modifier) {

    val viewModel: SuppliesViewModel = viewModel(
        factory = SuppliesViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
    val allProductDetails by viewModel.allProductDetail.collectAsState(initial = emptyList())

    val allProducts by viewModel.allProducts.collectAsState(emptyList())

    Column {
        RequirementScreen(modifier= modifier.weight(1f))

        LazyColumn(
            modifier = modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(allProductDetails) { detail ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = detail.productName, style = MaterialTheme.typography.titleMedium)
                            Text(text = detail.departmentName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(text = "수량: ${detail.quantity}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 12.dp))
                        val context = LocalContext.current
                        Button( onClick = {
                        //    Toast.makeText(context, "${detail.id}", Toast.LENGTH_LONG).show()
                            val productToDelete = allProducts.find { it.id == detail.id }
                            if (productToDelete != null){
                                viewModel.deleteProduct(productToDelete)
                                Toast.makeText(context,"${productToDelete.productName} deleted" ,Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context,"Product not found" ,Toast.LENGTH_LONG).show()
                            }
                        },
                            modifier = Modifier.padding(end = 12.dp),
                            colors = ButtonDefaults.buttonColors(Color( 0xFFFF9671))){ Text("삭제") }

                        Button( onClick = {
                            //Toast.makeText(context, "${detail.id}", Toast.LENGTH_LONG).show()
                            val productToUpdate = allProducts.find { it.id == detail.id}
                            if (productToUpdate != null){
                                //Toast.makeText(context, "${productToUpdate.productName} update", Toast.LENGTH_LONG).show()
                            }
                        },
                            modifier = Modifier.padding(end = 12.dp),
                            colors = ButtonDefaults.buttonColors(Color( 0xFF008E9B))){ Text("수정") }
                    }
                }
            }
        }


    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementScreen(modifier: Modifier) {

    val viewModel: SuppliesViewModel = viewModel(
        factory = SuppliesViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
    val allDepartments by viewModel.allDepartments.collectAsState(initial = emptyList())


    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedDepartment by remember { mutableStateOf<Department?>(null) }

    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") })
        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = selectedDepartment?.departmentName ?: "Select Department",
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                allDepartments.forEach { department ->
                    DropdownMenuItem(
                        text = { Text(department.departmentName) },
                        onClick = {
                            selectedDepartment = department
                            expanded = false
                        }
                    )
                }
            }
        }

        Button(onClick = {
            val qty = quantity.toIntOrNull() ?: 0
            selectedDepartment?.let { dept ->
                viewModel.insertProduct(Product(productName = productName, quantity = qty, departmentId = dept.id))
                // Clear fields after insert
                productName = ""
                quantity = ""
                selectedDepartment = null
            }
        }, enabled = productName.isNotBlank() && quantity.isNotBlank() && selectedDepartment != null) {
            Text("Submit Request")
        }
    }
}

@Composable
fun DepartmentScreen(modifier: Modifier) {

    var departmentName by remember { mutableStateOf("") }

    val viewModel: SuppliesViewModel = viewModel(
        factory = SuppliesViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add New Department", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = departmentName, onValueChange = { departmentName = it }, label = { Text("Department Name") })
        Button(onClick = {
            viewModel.insertDepartment(Department(departmentName = departmentName))
            // Clear fields after insert
            departmentName = ""
        }) {
            Text("Add Department")
        }
    }
}

@Composable
fun NaverSearchScreen(modifier: Modifier, navController: NavController) {
    val viewModel: NewsViewModel = viewModel()

    var searchQuery by rememberSaveable { mutableStateOf("가필드 고양이 캐릭터") }

//    val newsList by viewModel.newsList.collectAsState()


//   LaunchedEffect(Unit) {
//        if(newsList.isEmpty()) viewModel.naverFetchNews(searchQuery)
//    }

    val lazyNewsItems = viewModel.newsPagingData.collectAsLazyPagingItems()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("검색어 입력") },
                singleLine = true,
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.searchNews((searchQuery))
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.searchNews(searchQuery)
                }
            ) {
                Text("검색")
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = lazyNewsItems.itemCount,
                key = lazyNewsItems.itemKey { it.link }
               ) { index ->
                val news = lazyNewsItems[index]
                if (news != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                val encodedUrl = URLEncoder.encode(news.link, "UTF-8")
                                navController.navigate("${Screen.NaverSearchDetail.name}/$encodedUrl")
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(decodeHtml(news.title), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(decodeHtml(news.description), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(decodeHtml(news.pubDate), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }


            }
            lazyNewsItems.loadState.apply {
                when {
                    refresh is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center){
                                CircularProgressIndicator()
                            }
                        }
                    }
                    append is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }


        }
    }


}

@Composable
fun SwipeScreen(modifier: Modifier){
    val images = listOf(R.drawable.cats,R.drawable.catmozo, R.drawable.catfriends, R.drawable.catgoods)
    val pagerState = rememberPagerState(initialPage = 0){images.size}
    val scope = rememberCoroutineScope()

     Box(modifier = modifier){
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSize = PageSize.Fill,
            verticalAlignment = Alignment.CenterVertically,
            key = { images[it] },
        ){ index ->
            Image(
                painter = painterResource(images[index]),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

        }



    }

}


@Composable
fun InventoryUpdateScreen(modifier: Modifier) {

    val viewModel: SuppliesViewModel = viewModel(
        factory = SuppliesViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
    val allProductDetails by viewModel.allProductDetail.collectAsState(initial = emptyList())
    val allProducts by viewModel.allProducts.collectAsState(emptyList())
    val allDepartments by viewModel.allDepartments.collectAsState(initial = emptyList())

    var updateState by remember {  mutableStateOf(false) }


    Column {

            RequirementUpdateScreen(
                modifier = modifier.weight(1f),
                viewModel = viewModel
                )

        LazyColumn(
            modifier = modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(allProductDetails) { detail ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = detail.productName, style = MaterialTheme.typography.titleMedium)
                            Text(text = detail.departmentName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(text = "수량: ${detail.quantity}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 12.dp))
                        val context = LocalContext.current
                        Button( onClick = {
                            //    Toast.makeText(context, "${detail.id}", Toast.LENGTH_LONG).show()
                            val productToDelete = allProducts.find { it.id == detail.id }
                            if (productToDelete != null){
                                viewModel.deleteProduct(productToDelete)
                                Toast.makeText(context,"${productToDelete.productName} deleted" ,Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context,"Product not found" ,Toast.LENGTH_LONG).show()
                            }
                        },
                            modifier = Modifier.padding(end = 12.dp),
                            colors = ButtonDefaults.buttonColors(Color( 0xFFFF9671))){ Text("삭제") }

                        Button( onClick = {
                            //Toast.makeText(context, "${detail.id}", Toast.LENGTH_LONG).show()
                            val productToUpdate = allProducts.find { it.id == detail.id}
                            val departmentForProduct = allDepartments.find { it.departmentName == detail.departmentName }
                            if (productToUpdate != null){
                                productId = productToUpdate.id
                                productName = productToUpdate.productName
                                quantity = productToUpdate.quantity.toString()
                                selectedDepartment = departmentForProduct
                                SuppliesViewModel.updateState = true
                                //Toast.makeText(context, "${productToUpdate.productName} update", Toast.LENGTH_LONG).show()
                            }
                        },
                            modifier = Modifier.padding(end = 12.dp),
                            colors = ButtonDefaults.buttonColors(Color( 0xFF008E9B))){ Text("수정") }
                    }
                }
            }
        }


    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementUpdateScreen(
    modifier: Modifier,
    viewModel: SuppliesViewModel
    ) {
    var expanded by remember { mutableStateOf(false) }
    val allDepartments by viewModel.allDepartments.collectAsState(initial = emptyList())

    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") })
        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = selectedDepartment?.departmentName ?: "Select Department",
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                allDepartments.forEach { department ->
                    DropdownMenuItem(
                        text = { Text(department.departmentName) },
                        onClick = {
                            selectedDepartment = department
                            expanded = false
                        }
                    )
                }
            }
        }

        val context = LocalContext.current
        Button(onClick = {
            val qty = quantity.toIntOrNull() ?: 0
            selectedDepartment?.let { dept ->
                if(updateState == false) {
                    viewModel.insertProduct(Product(productName = productName, quantity = qty, departmentId = dept.id))
                    Toast.makeText(context,"insert",Toast.LENGTH_LONG).show()
                } else {
                    productId?.let { id ->
                        val productWidhId = Product(id = id, productName = productName, quantity = qty, departmentId = dept.id)
                        viewModel.updateProduct(productWidhId)
                        Toast.makeText(context,"update",Toast.LENGTH_LONG).show()
                    } ?: run {
                        Toast.makeText(context,"Error: id no found",Toast.LENGTH_LONG).show()
                    }


                }
                    // Clear fields after insert
                productName = ""
                quantity = ""
                selectedDepartment = null
                productId = 0
                updateState = false
            }
        },
            enabled = productName.isNotBlank() && quantity.isNotBlank() && selectedDepartment != null,
            colors = ButtonDefaults.buttonColors(Color( 0xFFff8c40))

        ) {
            Text("입력")
        }
    }
}

@Composable
fun MapPermission(modifier: Modifier) {
    var hasPermission by remember { mutableStateOf(false) }

    if (hasPermission) {
        KakaoMapScreen()
    } else {
        RequestLocationPermission {
            hasPermission = true
        }
    }
}


@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "위치 권한 허용됨", Toast.LENGTH_SHORT).show()
            onPermissionGranted()
        } else {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionGranted()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }
}




@Composable
fun KakaoMapScreen(
    modifier: Modifier = Modifier,
    latitude: Double = 37.5303,
    longitude: Double = 126.9664
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember { MapView(context) }

    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }



    // 지도 라이프사이클 관리
    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            Log.d("KakaoMap", "Map destroyed")
                        }

                        override fun onMapError(error: Exception?) {
                            Log.e("KakaoMap", "Map error: $error")
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun getPosition(): LatLng {
                            return LatLng.from(latitude, longitude)
                        }

                        override fun onMapReady(map: KakaoMap) {

                            kakaoMap = map

                            val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                                LatLng.from(latitude, longitude)
                            )
                            map.moveCamera(cameraUpdate)

                            // 제스처 활성화 예시
                            map.setGestureEnable(GestureType.Pan, true)     // 드래그 이동 허용
                            map.setGestureEnable(GestureType.Zoom, true)    // 핀치 줌 허용
                            map.setGestureEnable(GestureType.Rotate, true)  // 두 손가락 회전 허용
                            map.setGestureEnable(GestureType.Tilt, true)    // 기울이기 허용
                        }
                    }
                )
            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                mapView.resume()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                mapView.pause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                (mapView.parent as? ViewGroup)?.removeView(mapView)

            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { mapView }
        )
        FloatingActionButton(
            onClick = {
                val permission = Manifest.permission.ACCESS_FINE_LOCATION
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val currentLatLng = LatLng.from(it.latitude, it.longitude)
                                kakaoMap?.moveCamera(
                                    CameraUpdateFactory.newCenterPosition(currentLatLng)
                                )
                            } ?: run {
                                Toast.makeText(context, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "위치 권한이 허용되지 않았습니다.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "현재 위치로 이동"
            )
        }

    }

}

