/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author VEENA SHASTRI
 */
//One entry of a dropdown: the id goes to the database, the label is shown to the user

public class Item {
        public final String id;
        public final String label;

        public Item(String id, String label) {
            this.id=id;
            this.label=label;
        }

        //The dropdown displays whatever this returns, e.g. "3 - Pune Panthers"
        @Override
        public String toString() {
            return label.isEmpty() ? "" : id+" - "+label;
        }
}
