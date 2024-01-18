package org.traccar.api;

import org.traccar.model.Device;
import org.traccar.model.User;
import org.traccar.storage.query.Condition;

import java.util.LinkedList;
import java.util.List;

public class DevicePermission extends BaseObjectResource<Device>{

    public DevicePermission(Class<Device> baseClass) {
        super(baseClass);
    }

   public List<Condition> validaPermissoes(List<Device> devices){

       var conditions = new LinkedList<Condition>();
       conditions.add(new Condition.Permission(User.class, 1, baseClass).excludeGroups());
        return conditions;
    }
}
