package org.d3if3063.miniproject03.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.d3if3063.miniproject03.R
import org.d3if3063.miniproject03.ui.theme.MiniProject03Theme

@Composable
fun DisplayAlertDialog(
    openDialog: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    if (openDialog){
        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = stringResource(R.string.pesan_hapus))
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(onClick = { onConfirmation() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text(text = stringResource(R.string.tombol_hapus),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { onDismissRequest() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(text = stringResource(R.string.tombol_batal),
                            color = Color.Black
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun DisplayAlertDialogPreview() {
    MiniProject03Theme {
        DisplayAlertDialog(
            openDialog = true,
            onDismissRequest = {},
            onConfirmation = {}
        )
    }
}