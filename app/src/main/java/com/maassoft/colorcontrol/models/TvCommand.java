package com.maassoft.colorcontrol.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class TvCommand implements Parcelable {
    private String commandType;
    private String commandCode;
    private String brand;
    private String description;
    private int iconResId;
    private boolean requiresConfirmation;
    private int delayMs;
    private String category;
    
    public TvCommand() {
        // Default constructor
    }
    
    public TvCommand(String commandType, String commandCode, String brand) {
        this.commandType = commandType;
        this.commandCode = commandCode;
        this.brand = brand;
        this.delayMs = 0;
        this.requiresConfirmation = false;
        this.category = "BASIC";
    }
    
    public TvCommand(String commandType, String commandCode, String brand, String description) {
        this(commandType, commandCode, brand);
        this.description = description;
    }
    
    protected TvCommand(Parcel in) {
        commandType = in.readString();
        commandCode = in.readString();
        brand = in.readString();
        description = in.readString();
        iconResId = in.readInt();
        requiresConfirmation = in.readByte() != 0;
        delayMs = in.readInt();
        category = in.readString();
    }
    
    public static final Creator<TvCommand> CREATOR = new Creator<TvCommand>() {
        @Override
        public TvCommand createFromParcel(Parcel in) {
            return new TvCommand(in);
        }
        
        @Override
        public TvCommand[] newArray(int size) {
            return new TvCommand[size];
        }
    };
    
    // Getters and Setters
    public String getCommandType() {
        return commandType;
    }
    
    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
    
    public String getCommandCode() {
        return commandCode;
    }
    
    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getDescription() {
        return description != null ? description : commandType;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getIconResId() {
        return iconResId;
    }
    
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
    
    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }
    
    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }
    
    public int getDelayMs() {
        return delayMs;
    }
    
    public void setDelayMs(int delayMs) {
        this.delayMs = Math.max(0, delayMs);
    }
    
    public String getCategory() {
        return category != null ? category : "BASIC";
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    // Utility methods
    public boolean isValid() {
        return commandType != null && !commandType.isEmpty() &&
               commandCode != null && !commandCode.isEmpty() &&
               brand != null && !brand.isEmpty();
    }
    
    public boolean isForBrand(String targetBrand) {
        return brand.equalsIgnoreCase(targetBrand) || "ALL".equalsIgnoreCase(brand);
    }
    
    public String getDisplayName() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        // Convert command type to display name (e.g., "POWER" -> "Power")
        if (commandType != null && !commandType.isEmpty()) {
            return commandType.substring(0, 1).toUpperCase() + 
                   commandType.substring(1).toLowerCase().replace("_", " ");
        }
        return "Unknown Command";
    }
    
    // Factory methods for common commands
    public static TvCommand createPowerCommand(String brand, String code) {
        TvCommand command = new TvCommand("POWER", code, brand, "Power");
        command.setRequiresConfirmation(true);
        command.setCategory("POWER");
        return command;
    }
    
    public static TvCommand createVolumeUpCommand(String brand, String code) {
        return new TvCommand("VOLUME_UP", code, brand, "Volume Up");
    }
    
    public static TvCommand createVolumeDownCommand(String brand, String code) {
        return new TvCommand("VOLUME_DOWN", code, brand, "Volume Down");
    }
    
    public static TvCommand createMuteCommand(String brand, String code) {
        return new TvCommand("MUTE", code, brand, "Mute");
    }
    
    public static TvCommand createChannelUpCommand(String brand, String code) {
        return new TvCommand("CHANNEL_UP", code, brand, "Channel Up");
    }
    
    public static TvCommand createChannelDownCommand(String brand, String code) {
        return new TvCommand("CHANNEL_DOWN", code, brand, "Channel Down");
    }
    
    public static TvCommand createHomeCommand(String brand, String code) {
        return new TvCommand("HOME", code, brand, "Home");
    }
    
    public static TvCommand createBackCommand(String brand, String code) {
        return new TvCommand("BACK", code, brand, "Back");
    }
    
    public static TvCommand createMenuCommand(String brand, String code) {
        return new TvCommand("MENU", code, brand, "Menu");
    }
    
    @NonNull
    @Override
    public String toString() {
        return "TvCommand{" +
                "commandType='" + commandType + '\'' +
                ", commandCode='" + commandCode + '\'' +
                ", brand='" + brand + '\'' +
                ", description='" + description + '\'' +
                ", iconResId=" + iconResId +
                ", requiresConfirmation=" + requiresConfirmation +
                ", delayMs=" + delayMs +
                ", category='" + category + '\'' +
                '}';
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(commandType);
        dest.writeString(commandCode);
        dest.writeString(brand);
        dest.writeString(description);
        dest.writeInt(iconResId);
        dest.writeByte((byte) (requiresConfirmation ? 1 : 0));
        dest.writeInt(delayMs);
        dest.writeString(category);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TvCommand tvCommand = (TvCommand) o;
        
        if (!commandType.equals(tvCommand.commandType)) return false;
        return brand.equals(tvCommand.brand);
    }
    
    @Override
    public int hashCode() {
        int result = commandType.hashCode();
        result = 31 * result + brand.hashCode();
        return result;
    }
}