package org.wordpress.android.ui.mysite.cards.blaze

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wordpress.android.R
import org.wordpress.android.ui.compose.components.card.DashboardCard
import org.wordpress.android.ui.compose.styles.footerCTA
import org.wordpress.android.ui.compose.styles.smallTitle
import org.wordpress.android.ui.compose.styles.subTitle
import org.wordpress.android.ui.compose.utils.uiStringText
import org.wordpress.android.ui.mysite.MySiteCardAndItem.Card.DashboardCards.DashboardCard.BlazeCard.BlazeCampaignsCard

@Composable
@Suppress("FunctionName")
fun BlazeCampaignsCardView(
    modifier: Modifier = Modifier,
    blazeCampaignCard: BlazeCampaignsCard
) {
    DashboardCard(modifier = modifier, content = {
        Text(
            text = uiStringText(uiString = blazeCampaignCard.title),
            style = smallTitle(),
            textAlign = TextAlign.Start,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, top = 16.dp)
        )
        Text(
            text = uiStringText(uiString = blazeCampaignCard.campaign.status),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = colorResource(R.color.material_on_surface_emphasis_medium),
            textAlign = TextAlign.Start,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, top = 8.dp)
        )
        Text(
            text = uiStringText(uiString = blazeCampaignCard.campaign.title),
            style = subTitle(),
            textAlign = TextAlign.Start,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, top = 8.dp)
        )
        Text(
            text = uiStringText(uiString = blazeCampaignCard.footer.label),
            style = footerCTA(),
            textAlign = TextAlign.Start,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, top = 8.dp, bottom = 16.dp, end = 8.dp)
        )
    })
}
