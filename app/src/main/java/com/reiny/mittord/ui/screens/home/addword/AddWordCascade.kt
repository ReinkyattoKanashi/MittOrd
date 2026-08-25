package com.reiny.mittord.ui.screens.home.addword

/**
 * Entry cascade of the add-word panel.
 *
 * The rows fade in one after another, each [STEP] after the previous one, and the
 * focus request deliberately waits until the input fields are on screen - otherwise
 * the keyboard slides in over a panel that is still empty.
 *
 * The values are derived from each other on purpose: this coupling used to live only
 * in four hand-tuned literals, so moving one of them silently broke the focus timing.
 */
internal object AddWordCascade {

    /** One row of the cascade to the next. */
    private const val STEP = 110

    const val FADE_IN = 280
    const val FADE_OUT = 80
    const val SUBMIT_FADE_IN = 250

    const val TITLE_DELAY = 150
    const val WORD_FIELD_DELAY = TITLE_DELAY + STEP                 // 260
    const val TRANSLATION_FIELD_DELAY = WORD_FIELD_DELAY + STEP     // 370
    const val SUBMIT_DELAY = TRANSLATION_FIELD_DELAY + 90           // 460

    /** Fires between the translation field appearing and the submit button. */
    const val FOCUS_DELAY = TRANSLATION_FIELD_DELAY + 50            // 420

    /** Slide distance divisors: the title travels a shorter way than the fields. */
    const val TITLE_SLIDE_DIVISOR = 3
    const val FIELD_SLIDE_DIVISOR = 2
}
