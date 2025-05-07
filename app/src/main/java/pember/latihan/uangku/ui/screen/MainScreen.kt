package pember.latihan.uangku.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pember.latihan.uangku.ui.component.PieChart

@Composable
fun MainScreen() {
    val data = listOf(40f, 25f, 20f, 15f)
    val colors = listOf(Color.Blue, Color.Red, Color.Green, Color.Yellow)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Diagram Pengeluaran",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        PieChart(
            values = data,
            colors = colors,
            modifier = Modifier
                .size(300.dp)
        )
    }
}
