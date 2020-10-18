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

Or running from the jar directly:
```
java -jar efferalgang-radio-0.1.0.jar API_KEY=<YOUR_API_KEY> FRONTEND_HOST=https://heyalistair.github.io CHANNEL_ID=UCEhyiFmy5c6MrTY1iLz2bAQ ARCHIVE_PLAYLIST_ID=PLo8pUGGGB_pcVtFOKlSVc70sNFlP565Gr
```
