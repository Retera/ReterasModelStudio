## Copy of Reality Interactive's ImageIO TGA library

### Building

`ant build`

### Usage
Simply register the plugin with ImageIO:

``` java
IIORegistry registry = IIORegistry.getDefaultInstance();
registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());
```