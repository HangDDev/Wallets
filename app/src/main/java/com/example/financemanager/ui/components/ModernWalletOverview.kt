package com.example.financemanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financemanager.model.PaymentMethod
import java.text.NumberFormat

@Composable
fun ModernWalletOverview(walletBalances: Map<PaymentMethod, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Wallet Distribution",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            walletBalances.forEach { (wallet, balance) ->
                WalletBalanceRow(
                    wallet = wallet,
                    balance = balance,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun WalletBalanceRow(
    wallet: PaymentMethod,
    balance: Double,
    modifier: Modifier = Modifier
) {
    val (icon, walletName) = when (wallet) {
        PaymentMethod.CASH -> Pair(Icons.Default.Wallet, "Cash Wallet")
        PaymentMethod.ALIPAY -> Pair(Icons.Default.Payment, "Alipay")
        PaymentMethod.WECHAT -> Pair(Icons.Default.CurrencyExchange, "Wechat")
        PaymentMethod.OCTOPUS -> Pair(Icons.Default.AccountBalanceWallet, "Octopus Card")
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = walletName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = formatCurrency(balance),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (balance >= 0) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = java.util.Currency.getInstance("HKD")
    return format.format(amount)
}