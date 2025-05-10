# Counterplay

Prevents player death if they're above certain health threshold.

Damage types are fully compatible with both vanilla types and those added by datapacks/plugins.

```yaml
oneshot-protection:
  enabled: false # Set to true to enable
  health-threshold: 10.0 # Health threshold above which the player will not die
  excluded-damage-types: # Damage types that will not trigger the protection
  - generic_kill
  - fall
  - fly_into_wall
```

This is a concept based on the idea talked about in [this](https://youhavetrouble.me/blog/improving-minecraft/) blog post.
