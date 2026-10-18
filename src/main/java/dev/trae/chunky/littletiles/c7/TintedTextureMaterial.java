package dev.trae.chunky.littletiles.c7;

import se.llbit.chunky.resources.Texture;
import se.llbit.chunky.world.material.TextureMaterial;

/**
 * A TextureMaterial that applies ARGB color tinting.
 * 
 * Implements multiplicative blending: finalColor = textureColor * tintColor.
 * 
 * Based on LittleTiles color system:
 * - Color format: ARGB (0xAARRGGBB)
 * - Alpha: 255 = opaque, 0 = transparent
 * - Default: -1 (0xFFFFFFFF) = white = no tint
 */
public class TintedTextureMaterial extends TextureMaterial {
  
  private final int tintColor; // ARGB format
  
  // Pre-computed tint factors (0.0-1.0) to avoid division in getColor()
  private final float tintR;
  private final float tintG;
  private final float tintB;
  private final float tintA;
  
  /**
   * Create a tinted texture material.
   * 
   * @param texture the base Texture
   * @param tintColor ARGB color (0xAARRGGBB), -1 means no tint
   */
  public TintedTextureMaterial(Texture texture, int tintColor) {
    super(texture);
    this.tintColor = tintColor;
    
    // Extract and normalize ARGB components (0-255 → 0.0-1.0)
    this.tintA = ((tintColor >> 24) & 0xFF) / 255.0f;
    this.tintR = ((tintColor >> 16) & 0xFF) / 255.0f;
    this.tintG = ((tintColor >> 8) & 0xFF) / 255.0f;
    this.tintB = (tintColor & 0xFF) / 255.0f;
  }
  
  @Override
  public float[] getColor(double u, double v) {
    // Get base texture color at (u, v)
    float[] baseColor = texture.getColor(u, v);
    
    // Apply multiplicative tint: finalColor = textureColor * tintColor
    baseColor[0] *= tintR;  // Red
    baseColor[1] *= tintG;  // Green
    baseColor[2] *= tintB;  // Blue
    baseColor[3] *= tintA;  // Alpha
    
    return baseColor;
  }
  
  /**
   * Get the tint color in ARGB format (for debugging).
   */
  public int getTintColor() {
    return tintColor;
  }
  
  @Override
  public String toString() {
    return String.format("TintedTextureMaterial{texture=%s, tint=0x%08X}", 
        texture, tintColor);
  }
}
