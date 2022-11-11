package net.snipsniper.sceditor.stamps

import net.snipsniper.utils.InputContainer
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.KeyEvent

interface IStamp {
    fun update(input: InputContainer?, mouseWheelDirection: Int, keyEvent: KeyEvent?)
    fun render(g: Graphics, input: InputContainer, position: CCVector2Int, difference: Array<Double?>?, isSaveRender: Boolean, isCensor: Boolean, historyPoint: Int): Rectangle?

    fun editorUndo(historyPoint: Int)
    fun mousePressedEvent(button: Int, pressed: Boolean)
    fun reset()
    var width: Int
    var height: Int
    val iD: String?
    var color: CCColor?
    val type: StampType?
    fun addChangeListener(listener: IStampUpdateListener?)
    fun doAlwaysRender(): Boolean
}

interface IStampUpdateListener {
    //The type here is used when updating the EzMode UI for example
    //Obviously we only want to update the sliders if the input is not coming from the slider
    //The sliders cause SETTER, while editor input is triggered with INPUT
    //This way we only update sliders if necessary, which can prevent issues like
    //Endless loops when updating an element, it triggers a stamp update, which then triggers another update etc...
    //While JSlider.setValue() does not cause that, since it doesn't alert itself, JTextInput.setText does, causing an endless loop.
    //This is why I decided to use this enum, since in Stamps the only two ways to change the stamp is either by using the update() function
    //which uses an InputContainer or by using Setters.
    //In the case of the EzUI the EzModeSettingsCreator can then decide when to do what, which works great for now.
    enum class TYPE { SETTER, INPUT }
    fun updated(type: TYPE?)
}