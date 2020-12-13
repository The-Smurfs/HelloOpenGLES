#version 300 es

const mat3 yuv2rgb = mat3(
    1, 0, 1.2802,
    1, -0.214821, -0.380589,
    1, 2.127982, 0
);

precision highp float;
in vec4 outColor;
in vec2 outTexCoord;

uniform sampler2D yTexture;
uniform sampler2D uvTexture;
//uniform sampler2D vTexture;

out vec4 FragColor;

void main()
{
    float y, u, v, r, g, b;
    y = texture(yTexture, outTexCoord).r;
    u = texture(uvTexture, outTexCoord).a;
    v = texture(uvTexture, outTexCoord).r;
    // yuv to rgb
    /*y = 1.164 * (y - 16.0 / 255.0);
    u = u - 128.0 / 255.0;
    v = v - 128.0 / 255.0;

    r = y + 1.596 * v;
    g = y - 0.391 * u - 0.813 * v;
    b = y + 2.018 * u;*/

    u = u - 0.5;
    v = v - 0.5;
    r = y + 1.403 * v;
    g = y - 0.344 * u - 0.714 * v;
    b = y + 1.770 * u;

    FragColor = vec4(r, g, b, 1.0);
    /*vec3 yuv = vec3(
        1.1643 * (texture(yTexture, outTexCoord).r - 0.0625),
        texture(uvTexture, outTexCoord).a - 0.5,
        texture(uvTexture, outTexCoord).r - 0.5
    );
    vec3 rgb = yuv * yuv2rgb;
    FragColor = vec4(rgb, 1);*/
    //FragColor = texture(uTextureContainer, outTexCoord) * outColor;
    //FragColor = mix(texture(uTextureContainer, outTexCoord), texture(uTextureFace, outTexCoord), 0.2);
}