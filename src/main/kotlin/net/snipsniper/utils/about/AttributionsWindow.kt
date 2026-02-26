package net.snipsniper.utils.about

import net.snipsniper.utils.*
import java.awt.*
import java.net.URI
import javax.swing.*

class AttributionsWindow(
    parent: JFrame
): JFrame() {
    init {
        title = "Attributions"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = Dimension(graphicsConfiguration.bounds.size.width / 2, graphicsConfiguration.bounds.size.height / 2)

        val attributions = AttributionsLoader.load()

        val content = NonScrollJPanel(GridBagLayout()).apply {
            var yPos = 0
            //This way we dont need to write these everywhere. The gbc extension function supports passing defaults.
            val gbcFillH = gbc(fill = GridBagConstraints.HORIZONTAL, weightx = 1.0)

            attributions.thirdParty.forEach { attribution ->
                val license = attribution.license

                //Where the full license gets displayed.
                val licenseLabel = JTextArea().apply {
                    isVisible = false
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                }

                fun toggleLicenseLabel() {
                    licenseLabel.text = license.full
                    licenseLabel.isVisible = !licenseLabel.isVisible
                }

                //Open the authors website
                fun openWebsite() = Desktop.getDesktop().browse(URI(attribution.url))

                addNew(
                    component = JPanel(GridBagLayout()),
                    constraints = gbc(gridy = yPos++, defaults = gbcFillH)
                ) {
                    //Show the Attribution Name in the border
                    border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 4, true), attribution.name)
                    //Button Panel (Horizontal)
                    addNew(
                        component = JPanel(GridBagLayout()),
                        constraints = gbc(gridy = 0, defaults = gbcFillH)
                    ) {
                        addNew(
                            component = JButton("Website"),
                            constraints = gbc(gridx = 0)
                        ).onClick { openWebsite() }

                        addNew(
                            component = JButton(license.short),
                            constraints = gbc(gridx = 1)
                        ).onClick { toggleLicenseLabel() }

                        //Horizontal glue to make the buttons stick to the left. This fills up the empty space on the right.
                        addNew(
                            component = Box.createHorizontalGlue(),
                            constraints = gbc(gridx = 2, defaults = gbcFillH)
                        )
                    }
                    addNew(
                        component = licenseLabel,
                        constraints = gbc(gridy = 1, defaults = gbcFillH)
                    )
                }
            }
        }

        addNew(
            component = JScrollPane(content)
        ) {
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
            verticalScrollBar.unitIncrement = 32
        }

        centerOn(parent)
        isVisible = true

    }
}