//package org.easybangumi.next.shared.foundation.cartoon
//
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.combinedClickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import org.easybangumi.next.shared.foundation.image.AsyncImage
//import com.heyanle.easy_bangumi_cm.common.resources.Res
//import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover
//import dev.icerock.moko.resources.compose.painterResource
//import dev.icerock.moko.resources.compose.stringResource
//
///**
// * Created by heyanle on 2025/3/14.
// */
//
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun CartoonCardWithCover(
//    modifier: Modifier = Modifier,
//    star: Boolean = false,
//    cartoonCover: CartoonCover,
//    onClick: (CartoonCover) -> Unit,
//    onLongPress: ((CartoonCover) -> Unit)? = null,
//) {
//
//    Column(
//        modifier = modifier
//            .clip(RoundedCornerShape(4.dp))
//            .combinedClickable(
//                onClick = {
//                    onClick(cartoonCover)
//                },
//                onLongClick = {
//                    onLongPress?.invoke(cartoonCover)
//                }
//            )
//            .padding(4.dp),
//        horizontalAlignment = Alignment.Start,
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(19 / 27F)
//                .clip(RoundedCornerShape(4.dp)),
//        ) {
//            AsyncImage(
//                modifier = Modifier.fillMaxSize(),
//                model = cartoonCover.coverUrl,
//                contentDescription = cartoonCover.name,
//                error = painterResource(Res.images.empty_soyolin)
//            )
//            if (star) {
//
//                Text(
//                    fontSize = 13.sp,
//                    text = stringResource(Res.strings.stared_min),
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    modifier = Modifier
//                        .background(
//                            MaterialTheme.colorScheme.primary,
//                            RoundedCornerShape(0.dp, 0.dp, 4.dp, 0.dp)
//                        )
//                        .padding(4.dp, 0.dp)
//                )
//            }
//        }
//        Spacer(modifier = Modifier.size(4.dp))
//        Text(
//            style = MaterialTheme.typography.bodySmall,
//            text = cartoonCover.name,
//            maxLines = 4,
//            textAlign = TextAlign.Start,
//            overflow = TextOverflow.Ellipsis,
//        )
//        Spacer(modifier = Modifier.size(4.dp))
//    }
//}
