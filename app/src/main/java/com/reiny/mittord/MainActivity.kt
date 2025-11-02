package com.reiny.mittord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FileBasedFontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reiny.mittord.MainActivity.Companion.BOTTOM_BAR_BUTTON_SIZE
import com.reiny.mittord.ui.theme.MittOrdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MittOrdTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    Column(
                        Modifier.padding(
                            top = WindowInsets.statusBars.asPaddingValues()
                                .calculateTopPadding() + 24.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppLogo(Modifier.fillMaxWidth())
                    }
                }) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        EmptyState(Modifier.align(Alignment.Center).padding(bottom = 70.dp))

                        // Custom - Bottom Bar
                        FloatingBottomBar(false, {}, {}, {})
                    }
                }
            }
        }
    }

    companion object {
        val BOTTOM_BAR_BUTTON_SIZE = 34.dp
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    val sansation = FontFamily(Font(R.font.sansation_font))
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No words added", fontWeight = FontWeight.Bold, fontSize = 24.sp, fontFamily = sansation)
        Text("Tap the + button to add a new word", fontSize = 16.sp, fontFamily = sansation)
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Image(
        imageVector = ImageVector.vectorResource(id = R.drawable.app_logo),
        contentDescription = "App Logo",
        modifier = modifier
    )
}

@Composable
fun FloatingBottomBar(
    expanded: Boolean, onExpandToggle: () -> Unit, onLeftClick: () -> Unit, onRightClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth(0.85f)
                .height(70.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLeftClick) {
                    Icon(
                        Icons.Default.Home,
                        modifier = Modifier.size(BOTTOM_BAR_BUTTON_SIZE),
                        contentDescription = "Home"
                    )
                }
                Spacer(modifier = Modifier.width(56.dp))
                IconButton(
                    onClick = onRightClick
                ) {
                    Icon(
                        Icons.Default.Person,
                        modifier = Modifier.size(BOTTOM_BAR_BUTTON_SIZE),
                        contentDescription = "Profile"
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = onExpandToggle,
            shape = CircleShape,
            containerColor = Color.Black,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 19.dp)
                .size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FloatingBottomBarPreview() {
    MittOrdTheme {
        FloatingBottomBar(
            expanded = false,
            onExpandToggle = {},
            onLeftClick = {},
            onRightClick = {})
    }
}