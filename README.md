# Radio live

Gets the currently playing show from the any YouTube channel.

# Debug

```
gradle bootRun -Dagentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Pargs='API_KEY=<YOUR_API_KEY>,FRONTEND_HOST=http://localhost:3000,CHANNEL_ID=UC5Z2eMviso2vnK9iHnmJO8w'
```

# Run

```
gradle bootRun -Pargs='API_KEY=<YOUR_API_KEY>,FRONTEND_HOST=http://localhost:3000,CHANNEL_ID=UC5Z2eMviso2vnK9iHnmJO8w'
```