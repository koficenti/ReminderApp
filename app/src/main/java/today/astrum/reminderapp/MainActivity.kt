package today.astrum.reminderapp

import java.time.LocalDate
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import today.astrum.reminderapp.ui.theme.ReminderAppTheme
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.reflect.KProperty

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderAppTheme {
                Surface {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Calendar()
                        PopupSelector()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    ReminderAppTheme {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Calendar()
            }
        }
    }
}

class Modal : ViewModel(){
    val column = mutableStateOf(0)
    val title = mutableStateOf("")
    val opened = mutableStateOf(false)
    val options = mutableStateOf(listOf(""))
    var selected = mutableStateOf(mutableListOf(LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.ENGLISH), LocalDate.now().dayOfMonth.toString(), LocalDate.now().year.toString()))
}

@Composable
fun PopupSelector(){
    val modal: Modal = viewModel()
    val opened = modal.opened
    val options = modal.options

    if(!opened.value) return
    
    Dialog(onDismissRequest = { opened.value = false }, DialogProperties(dismissOnClickOutside = true)){
        Box(Modifier.background(Color.White, shape = RoundedCornerShape(5.dp)).padding(42.dp).height(300.dp)){
            Column (Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                Text(modal.title.value, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(20.dp))
                        for (option in options.value){
                            Box(Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(5.dp))
                                .padding(10.dp)
                                .width(100.dp)
                                .clickable {
                                    modal.opened.value = false
                                    var copy = modal.selected.value.toMutableList()
                                    copy[modal.column.value] = option
                                    modal.selected.value = copy
                                           },
                                contentAlignment = Alignment.Center)
                {
                                Text(option)
                            }
                            Spacer(Modifier.height(5.dp))
                        }
            }
        }
    }
}


// !TODO: Clean and organize this mess + Simplify
@Composable
fun Calendar(modifier: Modifier = Modifier) {

    val modal: Modal = viewModel()

    val month = modal.selected.value[0]
    val day = modal.selected.value[1]
    val year = modal.selected.value[2]

    val date = LocalDate.parse("${month} ${day} ${year}", DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH))
//    val date = LocalDate.now()
    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val days = (1..date.lengthOfMonth()).toList()

    fun calendarStart(): Int{
        return date.withDayOfMonth(1).dayOfWeek.value - 1
    }

    var dayOffset = calendarStart()
    
    Column(){
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            for(letter in arrayOf("M", "T", "W", "T", "F", "S", "S")){
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center){
                    Text(letter, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
        Row (horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            for (i in 0 until 7)
                Column (verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    for (j in 0 until 6){
                        val loc = j * 7 + i
                        if (loc >= dayOffset && loc < (date.lengthOfMonth() + dayOffset)){
                            val color = if (loc == dayOffset + (date.dayOfMonth - 1)){
                                MaterialTheme.colorScheme.tertiary
                            }
                            else {
                                MaterialTheme.colorScheme.primary
                            }
                            
                            Box(Modifier.size(36.dp).background(color = color, shape = RoundedCornerShape(5.dp)).clickable {
                                var copy = modal.selected.value.toMutableList()
                                copy[1] = (loc + 1 - dayOffset).toString()
                                modal.selected.value = copy
                            })
                        }else{
                            Box(Modifier.size(36.dp).background(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(5.dp)))
                        }
                    }
                }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.width(((36 * 7) + (11 * 6)).dp), horizontalArrangement = Arrangement.SpaceBetween){
            Button (onClick = {
                modal.title.value = "Please Select Month"
                modal.options.value = months;
                modal.column.value = 0
                modal.opened.value = true;
            }, Modifier.width((36 * 3).dp), shape = RoundedCornerShape(5.dp)) {
                Text(month.take(3), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }

            Button (modifier = Modifier.width(70.dp), onClick = {
                modal.title.value = "Please Select Day"
                modal.options.value = days.map{ it.toString() };
                modal.column.value = 1
                modal.opened.value = true;
            }, shape = RoundedCornerShape(5.dp)) {
                Text(day, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
            
            Button (onClick = {
                modal.title.value = "Please Select Year"
                modal.options.value = (LocalDate.now().year .. LocalDate.now().year + 10).toList().map {it.toString()};
                modal.column.value = 2
                modal.opened.value = true;
            }, Modifier.width((36 * 3).dp), SHAPE = RoundedCornerShape(5.dp)) {
                Text(year, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}