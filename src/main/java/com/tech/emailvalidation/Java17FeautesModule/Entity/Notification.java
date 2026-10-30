package com.tech.emailvalidation.Java17FeautesModule.Entity;
import java.util.*;
public  sealed interface Notification permits EmailNotification,SMSNotification,PushNotififcation,CustomeNotification
{
}
