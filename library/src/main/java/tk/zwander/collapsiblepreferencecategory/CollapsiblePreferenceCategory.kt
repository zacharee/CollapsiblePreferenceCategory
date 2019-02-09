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
import kotlinx.android.synthetic.main.pref_cat_collapsible.view.*

class CollapsiblePreferenceCategory(context: Context, attributeSet: AttributeSet) : PreferenceCategory(context, attributeSet) {
    var expanded = false
        set(value) {
            field = value

            wrappedGroup.isVisible = value

            if (!value) {
                generateSummary()
            } else {
                summary = null
            }
        }

    private val wrappedGroup = object : PreferenceCategory(context) {
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
        setIcon(R.drawable.arrow_up)

        val array = context.theme.obtainStyledAttributes(attributeSet, R.styleable.CollapsiblePreferenceCategory, 0, 0)
        expanded = array.getBoolean(R.styleable.CollapsiblePreferenceCategory_default_expanded, expanded)
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
        holder.itemView.text_holder.layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGING)
        }
    }

    override fun addPreference(preference: Preference): Boolean {
        return wrappedGroup.addPreference(preference)
    }

    override fun removePreference(preference: Preference): Boolean {
        return wrappedGroup.removePreference(preference)
    }

    private fun generateSummary() {
        val children = ArrayList<String>()

        for (i in 0 until wrappedGroup.preferenceCount) {
            children.add(wrappedGroup.getPreference(i).title?.toString() ?: continue)
        }

        summary = TextUtils.join(", ", children)
    }
}