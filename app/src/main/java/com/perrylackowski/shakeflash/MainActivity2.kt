package com.perrylackowski.shakeflash

//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.ViewModel
//
//// MVI State
//data class ViewState(
//    val isOn: Boolean = false,
//    val score: Float = 0.50f
//)
//
//// MVI Intent (User Actions)
//sealed class ViewIntent {
//    data class SetScore(val newScore: Float) : ViewIntent()
//    object ToggleIsOn : ViewIntent()
//}
//
//// MVI ViewModel (Handles state & processes intents)
//class MVIViewModel : ViewModel() {
//    private var _state by mutableStateOf(ViewState())
//    val state: ViewState get() = _state
//
//    fun processIntent(intent: ViewIntent) {
//        when (intent) {
//            is ViewIntent.SetScore -> {
//                _state = _state.copy(score = intent.newScore)
//            }
//            ViewIntent.ToggleIsOn -> {
//                _state = _state.copy(isOn = !_state.isOn)
//            }
//        }
//    }
//}
//
//// Composable View
//@Composable
//fun MVIView(viewModel: MVIViewModel = MVIViewModel()) {
////    val state = viewModel.state
//    val state = remember { mutableStateOf(viewModel.state) }
//    Column(
//        modifier = Modifier.fillMaxSize().padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = if (state.isOn) "State: ON" else "State: OFF",
//            style = MaterialTheme.typography.headlineMedium,
//            textAlign = TextAlign.Center,
//            modifier = Modifier
//                .clickable { viewModel.processIntent(ViewIntent.ToggleIsOn) }
//                .padding(16.dp)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text("Score: ${state.score}")
//
//        Slider(
//            value = state.score,
//            onValueChange = { viewModel.processIntent(ViewIntent.SetScore(it)) },
//            valueRange = 0.50f..100f
//        )
//    }
//}
//
//// Main Activity
//class MainActivity2 : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MaterialTheme {
//                MVIView()
//            }
//        }
//    }
//}
