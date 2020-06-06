package com.toxicglados.alcoholism.container;

import com.toxicglados.alcoholism.util.AlcoholismIntReferenceHolder;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IIntArray;

import javax.annotation.Nullable;

public abstract class AlcoholismContainer extends Container {
    protected AlcoholismContainer(@Nullable ContainerType<?> type, int id) {
        super(type, id);
    }

    // We use our own IntReferenceHolder class because the base class
    // has an issue where the "lastKnownValue" variable defaults to 0
    // which causes issues when we create a new container (which causes a new IntReferenceHolder to be created)
    // when the server thinks that the value of the int is 0
    // and the client thinks that the int is something else
    // because the server doesn't know to update the container on the client.
    // This is fixed by always syncing the first time a container is opened (see AlcoholismIntReferenceHolder)
    @Override
    protected void trackIntArray(IIntArray arrayIn) {
        for(int i = 0; i < arrayIn.size(); ++i) {
            this.trackInt(new AlcoholismIntReferenceHolder(arrayIn, i));
        }
    }
}
