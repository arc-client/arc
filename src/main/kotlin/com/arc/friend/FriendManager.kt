
package com.arc.friend

import com.arc.config.Configurable
import com.arc.config.configurations.FriendConfig
import com.arc.core.Loadable
import com.arc.util.text.ClickEvents
import com.arc.util.text.buildText
import com.arc.util.text.clickEvent
import com.arc.util.text.literal
import com.arc.util.text.styled
import com.arc.util.text.text
import com.mojang.authlib.GameProfile
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.text.Text
import java.awt.Color
import java.util.*

// ToDo:
//  - Allow adding of offline players by name or uuid.
//  - Should store the data until the player was seen.
//      Either no UUID but with name or no name but with uuid or both.
//      -> Should update the record if the player was seen again.
//  - Handle player changing names.
//  - Improve save file structure.
object FriendManager : Configurable(FriendConfig), Loadable {
    override val name = "friends"
    val friends by setting("friends", emptySet<GameProfile>())

    fun befriend(profile: GameProfile) = friends.add(profile)
    fun unfriend(profile: GameProfile): Boolean = friends.remove(profile)

    fun gameProfile(name: String) = friends.firstOrNull { it.name == name }
    fun gameProfile(uuid: UUID) = friends.firstOrNull { it.id == uuid }

    fun isFriend(profile: GameProfile) = friends.contains(profile)
    fun isFriend(name: String) = friends.any { it.name == name }
    fun isFriend(uuid: UUID) = friends.any { it.id == uuid }

    fun clear() = friends.clear()

    val OtherClientPlayerEntity.isFriend: Boolean
        get() = isFriend(gameProfile)

    fun OtherClientPlayerEntity.befriend() = befriend(gameProfile)
    fun OtherClientPlayerEntity.unfriend() = unfriend(gameProfile)

    override fun load() = "Loaded ${friends.size} friends"

    fun befriendedText(name: String): Text = befriendedText(Text.of(name))
    fun befriendedText(name: Text) = buildText {
        literal(Color.GREEN, "Added ")
        text(name)
        literal(" to your friend list ")
        clickEvent(ClickEvents.suggestCommand(";friends remove ${name.string}")) {
            styled(underlined = true, color = Color.LIGHT_GRAY) {
                literal("[Undo]")
            }
        }
    }

    fun unfriendedText(name: String): Text = unfriendedText(Text.of(name))
    fun unfriendedText(name: Text) = buildText {
        literal(Color.RED, "Removed ")
        text(name)
        literal(" from your friend list ")
        clickEvent(ClickEvents.suggestCommand(";friends add ${name.string}")) {
            styled(underlined = true, color = Color.LIGHT_GRAY) {
                literal("[Undo]")
            }
        }
    }
}
