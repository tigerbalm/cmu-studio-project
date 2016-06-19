# Request

## Topic

```
/facility/dbquery/get
```

## Body
```
{
  'session_key' : 'session_value' // owner's session key
  'dbquery_key' : 'sql_query // SQU Query statement to be executed
}
```

# Response

## Topic

```
/facility/dbquery/get/#
```

## Body

### Success

```
{
  'success': 1
  'result': SQL query result
}
```

### Failed

```
{
  'success': 0,
  'cause': 'INVALID_SESSION' // or other cause
}
```

# Note

If you use MQTT connection, following key/value is added in body automatically while exchanging data.
But when you received the message from channel, it does not exist.
Hence, you have to add and remove following pair to communicate with other library or entity. i.e. Arduino MQTT library.
If there is no this pair, the received element regards it as notification.


## Additional body for MQTT

### Notification (Publish/Subscribe) type message
```
{
  '_msg_type_' : 0 // Notification for publish message
}
```

### Request/Response type message
```
{
  '_msg_type_' : 1, or 2 // 1 : Request, 2 : Response.
}
```