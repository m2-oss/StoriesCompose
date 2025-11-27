package ru.m2.squaremeter.stories.container.presentation.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.m2.squaremeter.stories.container.presentation.ui.STEPPER_TRACK_COLOR
import ru.m2.squaremeter.stories.presentation.util.Colors

/**
 * A set of parameters for stories UI customization
 */
data class UiStoriesParams(
    /**
     * Considering status and navigation bar paddings. If set true, the space will be participating
     * in swipes between stories.
     * IMPORTANT:
     * 1. On older android platforms, don't forget to enable edge-to-edge mode in your activity, otherwise it doesn't work;
     * 2. Use [slideBackground] to set the colors of status and navigation bar, otherwise default color will be used.
     * @see <a href="https://developer.android.com/develop/ui/compose/system/setup-e2e">Edge-to-edge</a>
     */
    val fullScreen: Boolean = false,
    /**
     * Enabling 3d graphics transitions between stories. If set false, default behavior of swipes
     * will be used
     * @see <a href="https://developer.android.com/develop/ui/compose/layouts/pager#horizontalpager">Default swipe behavior</a>
     */
    val graphicsTransition: Boolean = true,
    /**
     * Enabling transparency of stories background. It helps see the previous screen during stories being closed.
     * IMPORTANT: It's up to you to make the stories screen transparent. E.g. you can use FragmentTransaction's add function.
     * @see <a href="https://developer.android.com/guide/fragments/transactions">Fragment transactions</a>
     */
    val transparentBackground: Boolean = false,
    /**
     * Paddings of the progress bar relative to its parent
     */
    val progressBarPaddings: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    /**
     * Arrangement between each progress bar item
     */
    val progressBarSpacedByArrangement: Dp = 4.dp,
    /**
     * Height of the progress bar
     */
    val progressBarHeight: Dp = 32.dp,
    /**
     * Height of each progress bar item
     */
    val progressHeight: Dp = 4.dp,
    /**
     * Shape of each progress bar item
     */
    val progressShape: Shape = RoundedCornerShape(8.dp),
    /**
     * Color of each unviewed progress bar item
     */
    val progressColor: Color = Colors.systemWhite,
    /**
     * Color of each viewed progress bar item
     */
    val progressTrackColor: Color = STEPPER_TRACK_COLOR,
    /**
     * Color background of a slide. Comes in handy with [fullScreen] to fill the screen with color
     * and change during swipes
     */
    val slideBackground: ((String, Int) -> Color)? = null
)
