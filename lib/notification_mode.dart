enum NotificationMode {
  backgroundOnly,
  silentOnly,
  backgroundAndForeground;

  int get number {
    switch (this) {
      case NotificationMode.backgroundOnly:
        return 0;
      case NotificationMode.silentOnly:
        return 1;
        case NotificationMode.backgroundAndForeground:
        return 2;
    }
  }

  bool get showInForeground {
    switch (this) {
      case NotificationMode.backgroundAndForeground:
        return true;
      case NotificationMode.backgroundOnly:
        return false;
      case NotificationMode.silentOnly:
        return false;
    }
  }
}
