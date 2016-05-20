/*
 * Copyright (C) 2016 Pragalathan M <pragalathanm@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.konsole.cluster;

import com.konsole.term.TerminalFactory;
import com.konsole.term.TerminalTopComponent;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.ELProperty;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
@ConvertAsProperties(
        dtd = "-//com.konsole.cluster//Cluster//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = CommandTopComponent.ID,
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "com.konsole.cluster.CommandTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_CommandAction",
        preferredID = "CommandTopComponent"
)
@Messages({
    "CTL_CommandAction=Commands",
    "CTL_CommandTopComponent=Commands",
    "HINT_CommandTopComponent=This shows the commands view"
})
public final class CommandTopComponent extends TopComponent {

    public static final String ID = "CommandTopComponent";
    private InstanceContent ic = new InstanceContent();
//    private static final RequestProcessor RP = new RequestProcessor(CommandTopComponent.class);

    public CommandTopComponent() {
        initComponents();
        setName(Bundle.CTL_CommandTopComponent());
        setToolTipText(Bundle.HINT_CommandTopComponent());
        associateLookup(new AbstractLookup(ic));
        commandTextArea.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ENTER_ACTION");
        commandTextArea.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "CTRL_L_ACTION");
        commandTextArea.getActionMap().put("ENTER_ACTION", ENTER_KEY_ACTION);
        commandTextArea.getActionMap().put("CTRL_L_ACTION", CTRL_L_ACTION);

//        WindowManager.getDefault().addPropertyChangeListener((PropertyChangeEvent evt) -> {
//            if ("modes".equals(evt.getPropertyName())) {
//                System.out.println("==================" + evt);
//                RP.post(() -> {
//                    SwingUtilities.invokeLater(() -> {
//                        Mode mode = WindowManager.getDefault().findMode(CommandTopComponent.this);
//                        if (mode != null) {
//                            System.err.println(mode + "mode =============== " + CommandTopComponent.this.getBounds());
//                        }
//                    });
//                }, 500);
//            }
//        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSplitPane(e.getComponent().getSize());
            }
        });
    }

    private void updateSplitPane(Dimension dim) {
        if (dim.height > 2 * dim.width || dim.width < 500) {
            splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        } else {
            splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setDividerLocation(dim.width - 300);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new BindingGroup();

        splitPane = new JSplitPane();
        jPanel1 = new JPanel();
        jScrollPane2 = new JScrollPane();
        commandTextArea = new JTextArea();
        runButton = new JButton();
        jPanel2 = new JPanel();
        jScrollPane1 = new JScrollPane();
        jList1 = new JList();

        setPreferredSize(new Dimension(300, 430));

        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);

        commandTextArea.setLineWrap(true);
        jScrollPane2.setViewportView(commandTextArea);

        Mnemonics.setLocalizedText(runButton, NbBundle.getMessage(CommandTopComponent.class, "CommandTopComponent.runButton.text")); // NOI18N

        Binding binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, commandTextArea, ELProperty.create("${text}"), runButton, BeanProperty.create("enabled"));
        binding.setConverter(STRING_TO_BOOLEAN_CONVERTER);
        bindingGroup.addBinding(binding);

        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(runButton)
                .addGap(0, 75, Short.MAX_VALUE))
            .addComponent(jScrollPane2)
        );

        splitPane.setTopComponent(jPanel1);

        jList1.setModel(new AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setPreferredSize(new Dimension(100, 85));
        jScrollPane1.setViewportView(jList1);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
        );

        splitPane.setRightComponent(jPanel2);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, GroupLayout.Alignment.TRAILING)
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void runButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        executeCommand(commandTextArea.getText());
    }//GEN-LAST:event_runButtonActionPerformed

    private void executeCommand(String command) {
        executeCommand(command, true);
    }

    private void executeCommand(String command, boolean clearTextField) {
        for (TerminalTopComponent openedTerminal : TerminalFactory.openedTerminals.values()) {
            openedTerminal.execute(command);
        }
        if (clearTextField) {
            commandTextArea.setText("");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextArea commandTextArea;
    private JList jList1;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JButton runButton;
    private JSplitPane splitPane;
    private BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {

    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private final Converter< String, Boolean> STRING_TO_BOOLEAN_CONVERTER = new Converter<String, Boolean>() {

        @Override
        public Boolean convertForward(String value) {
            return !value.isEmpty();
        }

        @Override
        public String convertReverse(Boolean value) {
            return "";
        }
    };

    private final Action ENTER_KEY_ACTION = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            executeCommand(commandTextArea.getText());
        }
    };
    private final Action CTRL_L_ACTION = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            executeCommand("clear", false);
        }
    };
}
