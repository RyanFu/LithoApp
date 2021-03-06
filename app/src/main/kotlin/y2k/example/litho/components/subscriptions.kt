package y2k.example.litho.components

import android.content.Intent
import android.graphics.Color
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Progress
import com.facebook.litho.widget.Recycler
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import y2k.example.litho.EntitiesActivity
import y2k.example.litho.RssSubscription
import y2k.example.litho.Subscriptions
import y2k.example.litho.launch
import y2k.example.litho.Loader as L

/**
 * Created by y2k on 06/07/2017.
 **/

@LayoutSpec
class MainComponentSpec {

    companion object {

        @OnUpdateState @JvmStatic
        fun reload(state: StateValue<Subscriptions>, @Param newState: Subscriptions) {
            state.set(newState)
        }

        @OnCreateInitialState @JvmStatic
        fun createInitialState(c: ComponentContext, state: StateValue<Subscriptions>) = launch {
            state.set(emptyList())

            L.getSubscriptions()
                .let { MainComponent.reload(c, it) }
        }

        @OnCreateLayout @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: Subscriptions): ComponentLayout {
            return when (state.isEmpty()) {
                true ->
                    Column.create(c)
                        .paddingDip(YogaEdge.ALL, 16)
                        .backgroundColor(Color.WHITE)
                        .child(Progress.create(c)
                            .color(Color.GRAY))
                        .build()
                false -> {
                    val recyclerBinder = RecyclerBinder(c)
                    state.forEachIndexed { i, x ->
                        recyclerBinder.insertItemAt(i, ItemComponent.create(c)
                            .item(x)
                            .build())
                    }

                    return Recycler.create(c)
                        .binder(recyclerBinder)
                        .buildWithLayout()
                }
            }
        }
    }
}

@LayoutSpec
class ItemComponentSpec {

    companion object {

        @JvmStatic @OnCreateLayout
        fun onCreateLayout(c: ComponentContext, @Prop item: RssSubscription): ComponentLayout {
            return Column.create(c)
                .paddingDip(YogaEdge.ALL, 16)
                .backgroundColor(Color.WHITE)
                .child(Text.create(c)
                    .text(item.title)
                    .textSizeSp(35f))
                .child(Text.create(c)
                    .text("" + item.url)
                    .textSizeSp(18f))
                .clickHandler(ItemComponent.onItemClicked(c, item))
                .build()
        }

        @OnEvent(ClickEvent::class) @JvmStatic
        fun onItemClicked(c: ComponentContext, @Param item: RssSubscription) {
            Intent(c, EntitiesActivity::class.java)
                .putExtra("data", item)
                .let { c.startActivity(it) }
        }
    }
}