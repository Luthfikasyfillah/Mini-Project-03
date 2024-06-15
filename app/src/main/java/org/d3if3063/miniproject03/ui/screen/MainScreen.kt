package org.d3if3063.miniproject03.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d3if3063.miniproject03.BuildConfig
import org.d3if3063.miniproject03.R
import org.d3if3063.miniproject03.model.Film
import org.d3if3063.miniproject03.model.User
import org.d3if3063.miniproject03.network.ApiStatus
import org.d3if3063.miniproject03.network.FilmApi
import org.d3if3063.miniproject03.network.UserDataStore
import org.d3if3063.miniproject03.ui.theme.MiniProject03Theme
import org.d3if3063.miniproject03.util.SettingsDataStore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val poppinsblack = FontFamily(Font(R.font.poppinsblack))

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showFilmDialog by remember { mutableStateOf(false) }

    val dataStore2 = SettingsDataStore(LocalContext.current)
    val showList by dataStore2.layoutFlow.collectAsState(true)
    
    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showFilmDialog = true
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    ) {
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                dataStore2.saveLayout(!showList)
                            }
                        }) {
                            Icon(
                                painter = painterResource(
                                    if (showList) R.drawable.view_list
                                    else R.drawable.grid_view
                                ),
                                contentDescription = stringResource(
                                    if (showList) R.string.list
                                    else R.string.grid
                                ),
                                tint = White
                            )
                        }

                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontFamily = poppinsblack,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = {
                            if (user.email.isEmpty()) {
                                CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                            } else {
                                showDialog = true
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.account_circle),
                                contentDescription = stringResource(R.string.profil),
                                tint = White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Black,
                    titleContentColor = White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val options = CropImageContractOptions(
                    null, CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = true,
                        fixAspectRatio = true
                    )
                )
                launcher.launch(options)
            },
                containerColor = Black
                ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.tambah_film),
                    tint = White
                )
            }
        }
    ){ padding ->
        ScreenContent(viewModel, user.email, Modifier.padding(padding), showList)

        if (showDialog){
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }
        if (showFilmDialog) {
            FilmDialog(
                bitmap = bitmap,
                onDismissRequest = { showFilmDialog = false }) { title, genre, year ->
                viewModel.saveData(user.email, title, genre, year, bitmap!!)
                showFilmDialog = false
            }
        }
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(viewModel: MainViewModel, userId: String, modifier: Modifier, showList: Boolean) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(userId) {
        viewModel.retrieveData(userId)
    }

    when (status) {
        ApiStatus.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        ApiStatus.SUCCESS -> {
            if (showList) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(data) { film ->
                        ListItem(film = film, onDelete = { filmId ->
                            Log.d("ScreenContent", "Deleting film with ID: $filmId")
                            viewModel.deleteData(userId, filmId)
                        })
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(data) { film ->
                        GridItem(film = film, onDelete = { filmId ->
                            Log.d("ScreenContent", "Deleting film with ID: $filmId")
                            viewModel.deleteData(userId, filmId)
                        })
                    }
                }
            }
        }
        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { viewModel.retrieveData(userId) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}


@Composable
fun ListItem(film: Film, onDelete: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    DisplayAlertDialog(
        openDialog = showDialog,
        onDismissRequest = { showDialog = false },
        onConfirmation = {
            onDelete(film.id)
            showDialog = false
        }
    )

    Row(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(8.dp))
            .background(color = White, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(FilmApi.getFilmUrl(film.imageId))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gambar, film.title),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.broken_image),
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = film.title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = film.genre,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = film.year,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        IconButton(onClick = {
            showDialog = true
        }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


@Composable
fun GridItem(film: Film, onDelete: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    DisplayAlertDialog(
        openDialog = showDialog,
        onDismissRequest = { showDialog = false },
        onConfirmation = {
            onDelete(film.id)
            showDialog = false
        }
    )

    Box(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, Color.Gray),
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(FilmApi.getFilmUrl(film.imageId))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gambar, film.title),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.broken_image),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = film.title,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = film.genre,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = film.year,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(onClick = {
                showDialog = true
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "",
                    tint = White
                )
            }
        }
    }
}



private suspend fun signIn(context: Context, dataStore: UserDataStore){
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context,request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException){
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
){
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException){
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    }
    else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore){
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException){
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful){
        Log.e("IMAGE","Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true )

@Composable
fun ScreenPreview() {
    MiniProject03Theme {
        MainScreen()
    }
}