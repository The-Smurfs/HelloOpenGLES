#version 300 es

const mat3 yuv2rgb = mat3(
    1, 0, 1.2802,
    1, -0.214821, -0.380589,
    1, 2.127982, 0
);

precision mediump float;
in vec4 outColor;
in vec2 outTexCoord;

uniform sampler2D uTextureContainer;
//uniform sampler2D uTextureFace;
uniform sampler2D mGLUniformTexture;
uniform sampler2D mGLUniformTexture1;

out vec4 FragColor;

void main()
{
    /*vec3 yuv = vec3(
        1.1643 * (texture2D(mGLUniformTexture, outTexCoord).r - 0.0625),
        texture2D(mGLUniformTexture1, textureCoordinate).a - 0.5,
        texture2D(mGLUniformTexture1, textureCoordinate).r - 0.5
    );
    vec3 rgb = yuv * yuv2rgb;
    gl_FragColor = vec4(rgb, 1);*/
    FragColor = texture(uTextureContainer, outTexCoord) * outColor;
    //FragColor = mix(texture(uTextureContainer, outTexCoord), texture(uTextureFace, outTexCoord), 0.2);
}