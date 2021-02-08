package tk.zwander.collapsiblepreferencecategory

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder

open class CollapsiblePreferenceCategory(context: Context, attributeSet: AttributeSet?) :
    PreferenceCategory(context, attributeSet) {

    var onExpandChangeListener: ((Boolean) -> Unit)? = null

    var expanded = false
        set(value) {
            field = value

            onExpandChangeListener?.invoke(value)

            wrappedGroup.isVisible = value

            generateSummary(value)

            if (!key.isNullOrBlank()) {
                persistBoolean(value)
            }
        }

    val wrappedGroup = object : PreferenceCategory(context) {
        init {
            layoutResource = R.layout.zero_height_pref
        }

        override fun onBindViewHolder(holder: PreferenceViewHolder) {
            super.onBindViewHolder(holder)

            holder.isDividerAllowedAbove = false
            holder.isDividerAllowedBelow = false
        }
    }

    init {
        layoutResource = R.layout.pref_cat_collapsible
        wrappedGroup.isOrderingAsAdded = this@CollapsiblePreferenceCategory.isOrderingAsAdded
        setIcon(R.drawable.arrow_up)

        if (attributeSet != null) {
            val array = context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.CollapsiblePreferenceCategory,
                0,
                0
            )
            expanded =
                array.getBoolean(R.styleable.CollapsiblePreferenceCategory_default_expanded, expanded)
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        expanded = run {
            val defBool = defaultValue?.toString()?.toBoolean() ?: false

            if (shouldPersist()) getPersistedBoolean(defBool)
            else defBool
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)

        if (!wrappedGroup.isAttached)
            super.addPreference(wrappedGroup)
    }

    override fun onAttached() {
        super.onAttached()

        expanded = expanded
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val icon = holder.itemView.findViewById<ImageView>(android.R.id.icon)
        icon.animate()
            .scaleY(if (expanded) 1f else -1f)
        holder.itemView.setOnClickListener {
            expanded = !expanded
        }
        holder.itemView.findViewById<ViewGroup>(R.id.collapsible_text_holder).layoutTransition =
            LayoutTransition().apply {
                enableTransitionType(LayoutTransition.CHANGING)
            }
    }

    @SuppressLint("RestrictedApi")
    override fun addPreference(preference: Preference): Boolean {
        return wrappedGroup.addPreference(preference)
    }

    @SuppressLint("RestrictedApi")
    override fun removePreference(preference: Preference): Boolean {
        return wrappedGroup.removePreference(preference)
    }

    fun generateSummary(expanded: Boolean = this.expanded) {
        if (wrappedGroup.preferenceCount > 0) {
            summary = if (!expanded) {
                val children = ArrayList<String>()

                for (i in 0 until wrappedGroup.preferenceCount) {
                    children.add(wrappedGroup.getPreference(i).title?.toString() ?: continue)
                }

                TextUtils.join(", ", children)
            } else {
                null
            }
        }
    }
}