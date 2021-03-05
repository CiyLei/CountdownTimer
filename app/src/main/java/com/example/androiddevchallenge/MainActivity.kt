/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotStarted
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlin.math.atan2
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MyApp() {
    val mainViewModel: MainViewModel = viewModel()
    Surface(color = MaterialTheme.colors.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            val second = mainViewModel.second.observeAsState(0)
            val running = mainViewModel.running.observeAsState(initial = false)
            Timer(second = second.value) {
                mainViewModel.second.value = it.toInt()
            }
            AnimatedVisibility(visible = running.value) {
                Text(text = second.value.toString(), color = Color.White, fontSize = 100.sp)
            }
            AnimatedVisibility(visible = !running.value) {
                IconButton(onClick = { mainViewModel.startCountdown() }) {
                    Icon(
                        imageVector = Icons.Rounded.NotStarted,
                        contentDescription = "start",
                        tint = Color.White,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
        }
    }
}

val backgroundColor = Color("#0984e3".toColorInt())
val ballColor = Color("#fdcb6e".toColorInt())
val ballSize = 30.dp
val ballBackGroundColor = Color("#d4237a".toColorInt())

@Composable
fun Timer(@IntRange(from = 0, to = 59) second: Int, onDrag: ((Float) -> Unit)? = null) {
    val mainViewModel: MainViewModel = viewModel()
    val ballOffsetX = remember { mutableStateOf(0f) }
    val ballOffsetY = remember { mutableStateOf(0f) }
    val ballTouchX = remember { mutableStateOf(0f) }
    val ballTouchY = remember { mutableStateOf(0f) }
    if (second == 0) {
        ballOffsetX.value = 0f
        ballOffsetY.value = 0f
        ballTouchX.value = 0f
        ballTouchY.value = 0f
    }

    BoxWithConstraints {
        val timerSize = min(maxWidth, maxHeight)
        Box(
            modifier = Modifier
                .size(timerSize)
                .background(backgroundColor, shape = RoundedCornerShape(timerSize / 2))
                .drawBehind {
                    drawArc(ballColor, -90f, (second / 60f) * 360f, true)
                    drawCircle(backgroundColor, size.minDimension / 2 - ballSize.toPx())
                },
            contentAlignment = Alignment.TopCenter
        ) {
            val running = mainViewModel.running.observeAsState(initial = false)
            if (!running.value) {
                Icon(
                    painter = painterResource(id = R.mipmap.time),
                    contentDescription = "timer",
                    modifier = Modifier
                        .offset { IntOffset(ballOffsetX.value.toInt(), ballOffsetY.value.toInt()) }
                        .background(ballBackGroundColor, RoundedCornerShape(ballSize / 2))
                        .size(ballSize)
                        .padding(5.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    ballTouchX.value = ballOffsetX.value
                                    ballTouchY.value = ballOffsetY.value
                                },
                                onDrag = { change, dragAmount ->
                                    change.consumeAllChanges()
                                    ballTouchX.value = ballTouchX.value + dragAmount.x
                                    ballTouchY.value = ballTouchY.value + dragAmount.y
                                    val r = timerSize.toPx() / 2 - ballSize.toPx() / 2
                                    val x = ballTouchX.value
                                    val y = (r - ballTouchY.value)
                                    if (x == 0f && y > 0f) {
                                        ballOffsetX.value = 0f
                                        ballOffsetY.value = 0f
                                    } else if (x == 0f && y < 0f) {
                                        ballOffsetX.value = 0f
                                        ballOffsetY.value = 2 * r
                                    } else if (y == 0f && x > 0f) {
                                        ballOffsetX.value = r
                                        ballOffsetY.value = 0f
                                    } else if (y == 0f && x < 0f) {
                                        ballOffsetX.value = -r
                                        ballOffsetY.value = 0f
                                    } else {
                                        val angle =
                                            atan2(
                                                (r - ballTouchY.value),
                                                ballTouchX.value
                                            )
                                        val k = y / x
                                        val x = sqrt(((r * r) / (1 + k * k)))
                                        val f =
                                            if (angle > Math.PI / 2 || angle < -Math.PI / 2) -1 else 1
                                        ballOffsetX.value = x * f
                                        ballOffsetY.value = r - x * k * f
                                        val percentage = (Math.PI - angle) / (2 * Math.PI)
                                        if (percentage < 0.25) {
                                            onDrag?.invoke(45f + 60 * percentage.toFloat())
                                        } else {
                                            onDrag?.invoke(60 * (percentage.toFloat() - 0.25f))
                                        }
                                    }
                                }
                            )
                        },
                    tint = Color.White
                )
            }
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
