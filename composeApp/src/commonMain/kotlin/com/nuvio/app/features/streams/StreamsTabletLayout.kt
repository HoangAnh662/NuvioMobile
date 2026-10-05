package com.nuvio.app.features.streams

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.shimmer
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TabletStreamsLayout(
    isEpisode: Boolean,
    title: String,
    logo: String?,
    poster: String?,
    background: String?,
    episodeThumbnail: String?,
    seasonNumber: Int?,
    episodeNumber: Int?,
    episodeTitle: String?,
    uiState: StreamsUiState,
    debridEnabled: Boolean,
    appendInstantServiceToDefaultName: Boolean,
    resumePositionMs: Long?,
    resumeProgressFraction: Float?,
    onStreamSelected: (stream: StreamItem, resumePositionMs: Long?, resumeProgressFraction: Float?) -> Unit,
    onStreamLongPress: (StreamItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val tabletBackdrop = remember(background, poster) { background ?: poster }
    var backdropVisible by remember(tabletBackdrop) { mutableStateOf(false) }

    LaunchedEffect(tabletBackdrop) {
        backdropVisible = tabletBackdrop == null
        if (tabletBackdrop != null) backdropVisible = true
    }

    val backdropAlpha by animateFloatAsState(
        targetValue = if (backdropVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 520),
        label = "tablet_backdrop_alpha",
    )
    val backdropScale by animateFloatAsState(
        targetValue = if (backdropVisible) 1f else 1.05f,
        animationSpec = tween(durationMillis = 620),
        label = "tablet_backdrop_scale",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)) {
            if (tabletBackdrop != null) {
                AsyncImage(
                    model = tabletBackdrop,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        alpha = backdropAlpha
                        scaleX = backdropScale
                        scaleY = backdropScale
                    },
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.6f),
                        ),
                    ),
                ),
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(0.45f).fillMaxHeight().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isEpisode && seasonNumber != null && episodeNumber != null) {
                    TabletEpisodeInfoPanel(logo, seasonNumber, episodeNumber, episodeTitle, title)
                } else {
                    TabletMovieInfoPanel(title, logo)
                }
            }

            // Outside-player source selector: use the same dark, clear glass language as the
            // in-player source sheet and let it reach the top/right/bottom screen edges.
            Box(
                modifier = Modifier.weight(0.55f).fillMaxHeight(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            shadowElevation = 20.dp.toPx()
                            shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
                            clip = false
                        }
                        .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                        .hazeEffect(state = hazeState) {
                            inputScale = HazeInputScale.Fixed(0.70f)
                            blurRadius = 58.dp
                        }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.Black.copy(alpha = 0.16f),
                                    Color.Black.copy(alpha = 0.30f),
                                ),
                            ),
                        ),
                ) {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.045f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.025f),
                                ),
                            ),
                        ),
                    )
                    Column(
                        modifier = Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 20.dp),
                    ) {
                        if ((resumePositionMs != null && resumePositionMs > 0L) || (resumeProgressFraction != null && resumeProgressFraction > 0f)) {
                            ResumeBanner(
                                positionMs = resumePositionMs,
                                progressFraction = resumeProgressFraction,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                        ProviderFilterRow(
                            groups = uiState.groups,
                            selectedFilter = uiState.selectedFilter,
                            onFilterSelected = { addonId -> StreamsRepository.selectFilter(addonId) },
                            onRefresh = onRefresh,
                        )
                        ActiveScrapersStatusBlock(uiState.groups, Modifier.padding(bottom = 4.dp))
                        StreamList(
                            uiState = uiState,
                            debridEnabled = debridEnabled,
                            appendInstantServiceToDefaultName = appendInstantServiceToDefaultName,
                            onStreamSelected = onStreamSelected,
                            onStreamLongPress = onStreamLongPress,
                            resumePositionMs = resumePositionMs,
                            resumeProgressFraction = resumeProgressFraction,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletMovieInfoPanel(title: String, logo: String?, modifier: Modifier = Modifier) {
    var logoLoadError by remember(logo) { mutableStateOf(false) }
    val logoUrl = logo?.takeIf { it.isNotBlank() }
    Column(
        modifier = modifier.fillMaxWidth(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (logoUrl != null && !logoLoadError) {
            AsyncImage(
                model = logoUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Fit,
                onError = { logoLoadError = true },
            )
        } else if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    shadow = Shadow(Color.Black.copy(alpha = 0.8f), Offset(0f, 2f), 4f),
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = stringResource(Res.string.streams_no_metadata),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TabletEpisodeInfoPanel(
    logo: String?,
    seasonNumber: Int,
    episodeNumber: Int,
    episodeTitle: String?,
    showTitle: String,
    modifier: Modifier = Modifier,
) {
    var logoLoadError by remember(logo) { mutableStateOf(false) }
    val logoUrl = logo?.takeIf { it.isNotBlank() }
    val textShadow = Shadow(Color.Black, Offset.Zero, 4f)
    Column(
        modifier = modifier.fillMaxWidth(0.8f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (logoUrl != null && !logoLoadError) {
            AsyncImage(
                model = logoUrl,
                contentDescription = showTitle,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Fit,
                onError = { logoLoadError = true },
            )
        } else {
            Text(
                text = showTitle,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    shadow = textShadow,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(
                Res.string.streams_episode_title_with_name,
                seasonNumber,
                episodeNumber,
                episodeTitle?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.streams_episode_fallback_title),
            ),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                shadow = textShadow,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ActiveScrapersStatusBlock(groups: List<AddonStreamGroup>, modifier: Modifier = Modifier) {
    val activeScrapers = remember(groups) { groups.filter { it.isLoading }.map { it.addonName }.distinct() }
    if (activeScrapers.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(Res.string.streams_active_scrapers),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            activeScrapers.forEach { addonName ->
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = addonName,
                        modifier = Modifier.shimmer(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
