package br.com.enali.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.com.enali.ui.theme.CorMarromEscuro
import br.com.enali.ui.theme.CorPreto

@Composable
fun CampoTextoCustomizado(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        visualTransformation = visualTransformation,
        singleLine = true,
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CorMarromEscuro,
            unfocusedBorderColor = Color.Gray,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = CorMarromEscuro,
            unfocusedLabelColor = Color.Gray,
            errorLabelColor = MaterialTheme.colorScheme.error,
            cursorColor = CorMarromEscuro,
            errorCursorColor = MaterialTheme.colorScheme.error,
            focusedTextColor = CorPreto,
            unfocusedTextColor = CorPreto,
            errorTextColor = CorPreto
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}