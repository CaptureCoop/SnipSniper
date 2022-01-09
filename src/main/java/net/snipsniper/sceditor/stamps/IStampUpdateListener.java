package net.snipsniper.sceditor.stamps;

public interface IStampUpdateListener {
    //The type here is used when updating the EzMode UI for example
    //Obviously we only want to update the sliders if the input is not coming from the slider
    //The sliders cause SETTER, while editor input is triggered with INPUT
    //This way we only update sliders if necessary, which can prevent issues like
    //Endless loops when updating an element, it triggers a stamp update, which then triggers another update etc...

    //While JSlider.setValue() does not cause that, since it doesn't alert itself, JTextInput.setText does, causing an endless loop.
    //This is why I decided to use this enum, since in Stamps the only two ways to change the stamp is either by using the update() function
    //which uses an InputContainer or by using Setters.
    //In the case of the EzUI the EzModeSettingsCreator can then decide when to do what, which works great for now.
    enum TYPE {SETTER, INPUT}
    void updated(TYPE type);
}
