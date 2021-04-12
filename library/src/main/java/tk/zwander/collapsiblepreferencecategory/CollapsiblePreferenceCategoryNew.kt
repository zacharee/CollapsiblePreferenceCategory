package tk.zwander.collapsiblepreferencecategory

import android.animation.LayoutTransition
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.view.isVisible
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import tk.zwander.collapsiblepreferencecategory.databinding.PrefCatCollapsibleBinding

open class CollapsiblePreferenceCategoryNew(context: Context, attrs: AttributeSet?) : PreferenceCategory(context, attrs) {
    enum class ArrowSide {
        START,
        END
    }

    var onExpandChangeListener: ((Boolean) -> Unit)? = null

    var expanded = false
        set(value) {
            field = value

            onExpandChangeListener?.invoke(value)

            for (i in 0 until preferenceCount) {
                getPreference(i).isVisible = value
            }

            generateSummary(value)

            if (!key.isNullOrBlank()) {
                persistBoolean(value)
            }
        }

    var arrowSide = ArrowSide.END
        set(value) {
            field = value

            notifyChanged()
        }

    init {
        layoutResource = R.layout.pref_cat_collapsible

        if (attrs != null) {
            val array = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CollapsiblePreferenceCategoryNew,
                0,
                0
            )

            expanded =
                array.getBoolean(R.styleable.CollapsiblePreferenceCategoryNew_default_expanded, expanded)

            val iconSide = array.getInt(R.styleable.CollapsiblePreferenceCategoryNew_arrow_side, arrowSide.ordinal)

            this.arrowSide = if (iconSide == ArrowSide.START.ordinal) ArrowSide.START else ArrowSide.END
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        expanded = run {
            val defBool = defaultValue?.toString()?.toBoolean() ?: false

            if (shouldPersist()) getPersistedBoolean(defBool)
            else defBool
        }
    }

    override fun addPreference(preference: Preference): Boolean {
        preference.isVisible = expanded
        return super.addPreference(preference)
    }

    override fun onAttached() {
        super.onAttached()

        generateSummary(expanded)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val binding = PrefCatCollapsibleBinding.bind(holder.itemView)

        val iconStart = binding.iconStart
        val iconEnd = binding.iconEnd
        val iconFrameStart = binding.collapsibleIconFrameStart
        val iconFrameEnd = binding.collapsibleIconFrameEnd

        iconStart.animate()
            .scaleY(if (expanded || arrowSide != ArrowSide.START) 1f else -1f)
        iconEnd.animate()
            .scaleY(if (expanded || arrowSide != ArrowSide.END) 1f else -1f)

        if (arrowSide == ArrowSide.START) {
            iconEnd.setImageDrawable(icon)
            if (isIconSpaceReserved) {
                iconFrameEnd.isVisible = true
            } else if (icon == null) {
                iconFrameEnd.isVisible = false
            }
            iconFrameStart.isVisible = true
            iconStart.setImageResource(R.drawable.arrow_up)
        } else {
            iconStart.setImageDrawable(icon)
            if (isIconSpaceReserved) {
                iconFrameStart.isVisible = true
            } else if (icon == null) {
                iconFrameStart.isVisible = false
            }
            iconFrameEnd.isVisible = true
            iconEnd.setImageResource(R.drawable.arrow_up)
        }

        holder.itemView.setOnClickListener {
            expanded = !expanded
        }
        binding.collapsibleTextHolder.layoutTransition =
            LayoutTransition().apply {
                enableTransitionType(LayoutTransition.CHANGING)
            }
    }

    fun generateSummary(expanded: Boolean = this.expanded) {
        if (preferenceCount > 0) {
            summary = if (!expanded) {
                val children = ArrayList<String>()

                for (i in 0 until preferenceCount) {
                    children.add(getPreference(i).title?.toString() ?: continue)
                }

                TextUtils.join(", ", children)
            } else {
                null
            }
        }
    }
}