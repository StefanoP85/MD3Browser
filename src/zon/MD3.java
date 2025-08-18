package zon;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MD3 {
    private static short ReadInt16(RandomAccessFile Source) throws IOException {
        byte[] ByteArray = new byte[2];
        Source.read(ByteArray);
        ByteBuffer RecordBuffer = ByteBuffer.wrap(ByteArray);
        RecordBuffer.order(ByteOrder.LITTLE_ENDIAN);
        short Result = RecordBuffer.asShortBuffer().get();
        return Result;
    }
    private static int ReadInt32(RandomAccessFile Source) throws IOException {
        byte[] ByteArray = new byte[4];
        Source.read(ByteArray);
        ByteBuffer RecordBuffer = ByteBuffer.wrap(ByteArray);
        RecordBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int Result = RecordBuffer.asIntBuffer().get();
        return Result;
    }
    private static float ReadFloat32(RandomAccessFile Source) throws IOException {
        byte[] ByteArray = new byte[4];
        Source.read(ByteArray);
        ByteBuffer RecordBuffer = ByteBuffer.wrap(ByteArray);
        RecordBuffer.order(ByteOrder.LITTLE_ENDIAN);
        float Result = RecordBuffer.asFloatBuffer().get();
        return Result;
    }
    private static String ReadString(RandomAccessFile Source, int NumberOfBytes) throws IOException {
        byte[] ByteArray = new byte[NumberOfBytes];
        Source.read(ByteArray);
        String Result = "";
        for (int I = 0; I < NumberOfBytes; I++) {
            if (ByteArray[I] > 0) {
                char Char = (char)ByteArray[I];
                Result = Result + Char;
            }
            else
                break;
        }
        int CharZeroPos = Result.indexOf('\0');
        if (CharZeroPos > 0)
            Result = Result.substring(0, CharZeroPos);
        return Result;
    }
    private static void WriteInt16(DataOutputStream Writer, short Value) throws IOException {
        byte[] ByteArray = new byte[2];
        ByteBuffer RecordBuffer = ByteBuffer.wrap(ByteArray);
        RecordBuffer.order(ByteOrder.LITTLE_ENDIAN);
        RecordBuffer.asShortBuffer().put(Value);
        Writer.write(ByteArray);
    }
    private static void WriteInt32(DataOutputStream Writer, int Value) throws IOException {
        byte[] ByteArray = new byte[4];
        ByteBuffer RecordBuffer = ByteBuffer.wrap(ByteArray);
        RecordBuffer.order(ByteOrder.LITTLE_ENDIAN);
        RecordBuffer.asIntBuffer().put(Value);
        Writer.write(ByteArray);
    }
    private static void WriteFloat32(DataOutputStream Writer, float Value) throws IOException {
        byte[] ByteArray = new byte[4];
        ByteBuffer RecordBuffer = ByteBuffer.wrap(ByteArray);
        RecordBuffer.order(ByteOrder.LITTLE_ENDIAN);
        RecordBuffer.asFloatBuffer().put(Value);
        Writer.write(ByteArray);
    }
    private static void WriteString(DataOutputStream Writer, String Value, int NumberOfBytes) throws IOException {
        byte[] ByteArray = new byte[NumberOfBytes];
        for (int I = 0; I < NumberOfBytes; I++)
            ByteArray[I] = 0;
        int CharLimit = Value.length();
        if (CharLimit > 63)
            CharLimit = 63;
        for (int I = 0; I < CharLimit; I++) {
            char Char = Value.charAt(I);
            short CodePoint = (short)Char;
            if ((CodePoint > 32) && (CodePoint < 127))
                ByteArray[I] = (byte)CodePoint;
            else
                ByteArray[I] = 95; // "_" character.
        }
        Writer.write(ByteArray);
    }
    private static boolean LoadFromBinary(TMD3Model MD3Model, String FileName) {
        try (RandomAccessFile Source = new RandomAccessFile(FileName, "r")) {
            // Read the header.
            MD3Model.HeaderID = ReadString(Source, 4);
            if (!MD3Model.HeaderID.equals("IDP3"))
                return false;
            MD3Model.HeaderVersion = ReadInt32(Source);
            MD3Model.HeaderName = ReadString(Source, 64);
            MD3Model.Flags = ReadInt32(Source);
            MD3Model.NumberOfFrames = ReadInt32(Source);
            MD3Model.NumberOfTags = ReadInt32(Source);
            MD3Model.NumberOfSurfaces = ReadInt32(Source);
            MD3Model.NumberOfSkins = ReadInt32(Source);
            int OffsetOfFrames = ReadInt32(Source);
            int OffsetOfTags = ReadInt32(Source);
            int OffsetOfSurfaces = ReadInt32(Source);
            int OffsetEnd = ReadInt32(Source);
            // Read the frames.
            Source.seek(OffsetOfFrames);
            for (int FrameIndex = 0; FrameIndex < MD3Model.NumberOfFrames; FrameIndex++) {
                float X = ReadFloat32(Source);
                float Y = ReadFloat32(Source);
                float Z = ReadFloat32(Source);
                TCoordinate MinBound = new TCoordinate(X, Y, Z);
                X = ReadFloat32(Source);
                Y = ReadFloat32(Source);
                Z = ReadFloat32(Source);
                TCoordinate MaxBound = new TCoordinate(X, Y, Z);
                X = ReadFloat32(Source);
                Y = ReadFloat32(Source);
                Z = ReadFloat32(Source);
                TCoordinate LocalOrigin = new TCoordinate(X, Y, Z);
                float Radius = ReadFloat32(Source);
                String Name = ReadString(Source, 16);
                TMD3Frame MD3ModelFrame = new TMD3Frame(MinBound, MaxBound, LocalOrigin, Radius, Name);
                MD3Model.Frames.add(MD3ModelFrame);
            }
            // Read the tags.
            Source.seek(OffsetOfTags);
            for (int FrameIndex = 0; FrameIndex < MD3Model.NumberOfFrames; FrameIndex++) {
                for (int TagIndex = 0; TagIndex < MD3Model.NumberOfTags; TagIndex++) {
                    String Name = ReadString(Source, 64);
                    float X = ReadFloat32(Source);
                    float Y = ReadFloat32(Source);
                    float Z = ReadFloat32(Source);
                    TCoordinate Location = new TCoordinate(X, Y, Z);
                    X = ReadFloat32(Source);
                    Y = ReadFloat32(Source);
                    Z = ReadFloat32(Source);
                    TCoordinate OrientationX = new TCoordinate(X, Y, Z);
                    X = ReadFloat32(Source);
                    Y = ReadFloat32(Source);
                    Z = ReadFloat32(Source);
                    TCoordinate OrientationY = new TCoordinate(X, Y, Z);
                    X = ReadFloat32(Source);
                    Y = ReadFloat32(Source);
                    Z = ReadFloat32(Source);
                    TCoordinate OrientationZ = new TCoordinate(X, Y, Z);
                    if (FrameIndex == 0) {
                        TMD3Tag MD3Tag = new TMD3Tag(Name);
                        MD3Model.Tags.add(MD3Tag);
                    }
                    TMD3TagFrame MD3FrameTag = new TMD3TagFrame(Name, Location, OrientationX, OrientationY, OrientationZ);
                    MD3Model.TagFrames.add(MD3FrameTag);
                }
            }
            // Read the surfaces.
            Source.seek(OffsetOfSurfaces);
            int CurrentSurfaceOffset;
            for (int SurfaceIndex = 0; SurfaceIndex < MD3Model.NumberOfSurfaces; SurfaceIndex++) {
                CurrentSurfaceOffset = (int)Source.getFilePointer();
                String SurfaceID = ReadString(Source, 4);
                if (!SurfaceID.equals("IDP3"))
                    return false;
                String SurfaceName = ReadString(Source, 64);
                int Flags = ReadInt32(Source);
                int NumberOfFrames = ReadInt32(Source);
                int NumberOfShaders = ReadInt32(Source);
                int NumberOfVertices = ReadInt32(Source);
                int NumberOfTriangles = ReadInt32(Source);
                int OffsetOfTriangles = ReadInt32(Source);
                int OffsetOfShaders = ReadInt32(Source);
                int OffsetOfTextVerts = ReadInt32(Source);
                int OffsetOfVertices = ReadInt32(Source);
                int SurfaceOffsetEnd = ReadInt32(Source);
                TMD3Surface MD3Surface = new TMD3Surface(SurfaceID, SurfaceName, Flags);
                // Read the shaders.
                Source.seek(CurrentSurfaceOffset + OffsetOfShaders);
                for (int ShaderIndex = 0; ShaderIndex < NumberOfShaders; ShaderIndex++) {
                    String ShaderName = ReadString(Source, 64);
                    int Index = ReadInt32(Source);
                    TMD3Shader MD3Shader = new TMD3Shader(ShaderName, Index);
                    MD3Surface.MD3Shaders.add(MD3Shader);
                }
                // Read the triangles.
                Source.seek(CurrentSurfaceOffset + OffsetOfTriangles);
                for (int TriangleIndex = 0; TriangleIndex < NumberOfTriangles; TriangleIndex++) {
                    int VerticeIndex0 = ReadInt32(Source);
                    int VerticeIndex1 = ReadInt32(Source);
                    int VerticeIndex2 = ReadInt32(Source);
                    TMD3Triangle MD3Triangle = new TMD3Triangle(VerticeIndex0, VerticeIndex1, VerticeIndex2);
                    MD3Surface.MD3Triangles.add(MD3Triangle);
                }
                // Read the texture coordinates.
                Source.seek(CurrentSurfaceOffset + OffsetOfTextVerts);
                for (int VerticeIndex = 0; VerticeIndex < NumberOfVertices; VerticeIndex++) {
                    float U = ReadFloat32(Source);
                    float V = ReadFloat32(Source);
                    TMD3TextVertex MD3TextVertex = new TMD3TextVertex(U, V);
                    MD3Surface.MD3TextVertexes.add(MD3TextVertex);
                }
                // Read the space coordinates.
                Source.seek(CurrentSurfaceOffset + OffsetOfVertices);
                for (int VerticeIndex = 0; VerticeIndex < NumberOfVertices * NumberOfFrames; VerticeIndex++) {
                    short X = ReadInt16(Source);
                    short Y = ReadInt16(Source);
                    short Z = ReadInt16(Source);
                    byte Latitude = Source.readByte();
                    byte Longitude = Source.readByte();
                    TMD3Vertex MD3Vertex = new TMD3Vertex(X, Y, Z, Latitude, Longitude);
                    MD3Surface.MD3Vertexes.add(MD3Vertex);
                }
                // End of surface.
                MD3Model.Surfaces.add(MD3Surface);
                Source.seek(CurrentSurfaceOffset + SurfaceOffsetEnd);
            }
            return true;
        } catch (IOException Ignored) {
            return false;
        }
    }
    private static boolean LoadFromText(TMD3Model MD3Model, String FileName) {
        TMD3Surface MD3Surface = null;
        TMD3Tag MD3Tag = null;
        TMD3TagFrame MD3TagFrame = null;
        boolean TagPositionRead = false;
        boolean TagRotationRead = false;
        MD3Model.HeaderID = "IDP3";
        MD3Model.HeaderVersion = 15;
        MD3Model.HeaderName = "";
        try (BufferedReader Source = new BufferedReader(new FileReader(FileName))) {
            String InputLine;
            while ((InputLine = Source.readLine()) != null) {
                // Remove the comments.
                int CommentPos = InputLine.indexOf('#');
                if ((CommentPos == 0) || (InputLine.length() == 0))
                    continue;
                if (CommentPos > 0)
                    InputLine = InputLine.substring(0, CommentPos - 1);
                String[] InputTokens = InputLine.split("\\s");
                if (InputTokens.length > 0) {
                    if (InputTokens[0].equalsIgnoreCase("name")) {
                        if (InputTokens.length > 1)
                            MD3Model.HeaderName = InputTokens[1];
                    }
                    if (InputTokens[0].equalsIgnoreCase("frame")) {
                        TCoordinate MinBound = new TCoordinate(0, 0, 0);
                        TCoordinate MaxBound = new TCoordinate(0, 0, 0);
                        TCoordinate LocalOrigin = new TCoordinate(0, 0, 0);
                        float Radius = 0;
                        String Name = "";
                        if (InputTokens.length > 1)
                            Name = InputTokens[1];
                        TMD3Frame MD3ModelFrame = new TMD3Frame(MinBound, MaxBound, LocalOrigin, Radius, Name);
                        MD3Model.Frames.add(MD3ModelFrame);
                        MD3Model.NumberOfFrames++;
                    }
                    if (InputTokens[0].equalsIgnoreCase("tag")) {
                        String Name = "";
                        if (InputTokens.length > 1)
                            Name = InputTokens[1];
                        MD3Tag = new TMD3Tag(Name);
                        TagPositionRead = false;
                        TagRotationRead = false;
                        MD3Model.Tags.add(MD3Tag);
                        MD3Model.NumberOfTags++;
                    }
                    if (InputTokens[0].equalsIgnoreCase("tp")) {
                        TagPositionRead = true;
                        if (!TagRotationRead) {
                            MD3TagFrame = new TMD3TagFrame(MD3Tag.Name,
                                    new TCoordinate(0, 0, 0),
                                    new TCoordinate(0, 0, 0),
                                    new TCoordinate(0, 0, 0),
                                    new TCoordinate(0, 0, 0)
                            );
                        }
                        MD3TagFrame.Location.X = Float.parseFloat(InputTokens[1]);
                        MD3TagFrame.Location.Y = Float.parseFloat(InputTokens[2]);
                        MD3TagFrame.Location.Z = Float.parseFloat(InputTokens[3]);
                        if (TagRotationRead) {
                            MD3Model.TagFrames.add(MD3TagFrame);
                            TagPositionRead = false;
                            TagRotationRead = false;
                        }
                    }
                    if (InputTokens[0].equalsIgnoreCase("tr")) {
                        TagRotationRead = true;
                        if (!TagPositionRead) {
                            MD3TagFrame = new TMD3TagFrame(MD3Tag.Name,
                                    new TCoordinate(0, 0, 0),
                                    new TCoordinate(0, 0, 0),
                                    new TCoordinate(0, 0, 0),
                                    new TCoordinate(0, 0, 0)
                            );
                        }
                        MD3TagFrame.OrientationX.X = Float.parseFloat(InputTokens[1]);
                        MD3TagFrame.OrientationX.Y = Float.parseFloat(InputTokens[2]);
                        MD3TagFrame.OrientationX.Z = Float.parseFloat(InputTokens[3]);
                        MD3TagFrame.OrientationY.X = Float.parseFloat(InputTokens[4]);
                        MD3TagFrame.OrientationY.Y = Float.parseFloat(InputTokens[5]);
                        MD3TagFrame.OrientationY.Z = Float.parseFloat(InputTokens[6]);
                        MD3TagFrame.OrientationZ.X = Float.parseFloat(InputTokens[7]);
                        MD3TagFrame.OrientationZ.Y = Float.parseFloat(InputTokens[8]);
                        MD3TagFrame.OrientationZ.Z = Float.parseFloat(InputTokens[9]);
                        if (TagPositionRead) {
                            MD3Model.TagFrames.add(MD3TagFrame);
                            TagPositionRead = false;
                            TagRotationRead = false;
                        }
                    }
                    if (InputTokens[0].equalsIgnoreCase("mesh")) {
                        String Name = "";
                        if (InputTokens.length > 1)
                            Name = InputTokens[1];
                        MD3Surface = new TMD3Surface("IDP3", Name, 0);
                        MD3Model.Surfaces.add(MD3Surface);
                        MD3Model.NumberOfSurfaces++;
                    }
                    if (InputTokens[0].equalsIgnoreCase("material")) {
                        String Name = "";
                        if (InputTokens.length > 1)
                            Name = InputTokens[1];
                        TMD3Shader MD3Shader = new TMD3Shader(Name, 0);
                        MD3Surface.MD3Shaders.add(MD3Shader);
                    }
                    if (InputTokens[0].equalsIgnoreCase("fm")) {
                        int I0 = Integer.parseInt(InputTokens[1]);
                        int I1 = Integer.parseInt(InputTokens[2]);
                        int I2 = Integer.parseInt(InputTokens[3]);
                        TMD3Triangle MD3Triangle = new TMD3Triangle(I0, I1, I2);
                        MD3Surface.MD3Triangles.add(MD3Triangle);
                    }
                    if (InputTokens[0].equalsIgnoreCase("vp")) {
                        short X = Short.parseShort(InputTokens[1]);
                        short Y = Short.parseShort(InputTokens[2]);
                        short Z = Short.parseShort(InputTokens[3]);
                        byte Latitude = Byte.parseByte(InputTokens[4]);
                        byte Longitude = Byte.parseByte(InputTokens[5]);
                        TMD3Vertex MD3Vertex = new TMD3Vertex(X, Y, Z, Latitude, Longitude);
                        MD3Surface.MD3Vertexes.add(MD3Vertex);
                    }
                    if (InputTokens[0].equalsIgnoreCase("vt")) {
                        float U = Float.parseFloat(InputTokens[1]);
                        float V = Float.parseFloat(InputTokens[2]);
                        TMD3TextVertex MD3TextVertex = new TMD3TextVertex(U, V);
                        MD3Surface.MD3TextVertexes.add(MD3TextVertex);
                    }
                }
            }
            // Process frames.
            for (int FrameIndex = 0; FrameIndex < MD3Model.NumberOfFrames; FrameIndex++) {
                TMD3Frame MD3ModelFrame = MD3Model.Frames.get(FrameIndex);
                float MaxX = -655360;
                float MaxY = -655360;
                float MaxZ = -655360;
                float MinX = 655360;
                float MinY = 655360;
                float MinZ = 655360;
                for (int SurfaceIndex = 0; SurfaceIndex < MD3Model.NumberOfSurfaces; SurfaceIndex++) {
                    MD3Surface = MD3Model.Surfaces.get(SurfaceIndex);
                    int NumberOfVertexes = MD3Surface.MD3TextVertexes.size();
                    int FirstIndex = FrameIndex * NumberOfVertexes;
                    int LastIndex = FirstIndex + NumberOfVertexes;
                    for (int VertexIndex = FirstIndex; VertexIndex < LastIndex; VertexIndex++) {
                        short X = MD3Surface.MD3Vertexes.get(VertexIndex).X;
                        short Y = MD3Surface.MD3Vertexes.get(VertexIndex).Y;
                        short Z = MD3Surface.MD3Vertexes.get(VertexIndex).Z;
                        if (X > MaxX)
                            MaxX = X;
                        if (Y > MaxY)
                            MaxY = Y;
                        if (Z > MaxZ)
                            MaxZ = Z;
                        if (X < MinX)
                            MinX = X;
                        if (Y < MinY)
                            MinY = Y;
                        if (Z < MinZ)
                            MinZ = Z;
                    }
                }
                MD3ModelFrame.MaxBound.X = MaxX;
                MD3ModelFrame.MaxBound.Y = MaxY;
                MD3ModelFrame.MaxBound.Z = MaxZ;
                MD3ModelFrame.MinBound.X = MinX;
                MD3ModelFrame.MinBound.Y = MinY;
                MD3ModelFrame.MinBound.Z = MinZ;
                float DX = MD3ModelFrame.MaxBound.X - MD3ModelFrame.MinBound.X;
                float DY = MD3ModelFrame.MaxBound.Y - MD3ModelFrame.MinBound.Y;
                float DZ = MD3ModelFrame.MaxBound.Z - MD3ModelFrame.MinBound.Z;
                MD3ModelFrame.Radius = (float)Math.sqrt(DX * DX + DY * DY + DZ * DZ);
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException | IOException E) {
            return false;
        }
    }
    private static boolean SaveToBinary(TMD3Model MD3Model, String FileName) {
        int OffsetOfFrames = 108;
        int OffsetOfTags = OffsetOfFrames + MD3Model.NumberOfFrames * 56;
        int OffsetOfSurfaces = OffsetOfTags + MD3Model.NumberOfTags * MD3Model.NumberOfFrames * 112;
        int SurfaceSize = 0;
        for (int SurfaceIndex = 0; SurfaceIndex < MD3Model.NumberOfSurfaces; SurfaceIndex++) {
            TMD3Surface MD3Surface =MD3Model.Surfaces.get(SurfaceIndex);
            SurfaceSize +=
                    108 + // Surface header size.
                    MD3Surface.MD3Triangles.size() * 12 + // Triangles array size.
                    MD3Surface.MD3Shaders.size() * 68 + // Shaders array size.
                    MD3Surface.MD3TextVertexes.size() * 8 + // Vertices array size.
                    MD3Surface.MD3TextVertexes.size() * MD3Model.Frames.size() * 8; // Frames vertices array size.
        }
        int OffsetEnd = OffsetOfSurfaces + SurfaceSize;
        try (DataOutputStream Writer = new DataOutputStream(new FileOutputStream(FileName))) {
            WriteString(Writer, MD3Model.HeaderID, 4);
            WriteInt32(Writer, MD3Model.HeaderVersion);
            WriteString(Writer, MD3Model.HeaderName, 64);
            WriteInt32(Writer, MD3Model.Flags);
            WriteInt32(Writer, MD3Model.NumberOfFrames);
            WriteInt32(Writer, MD3Model.NumberOfTags);
            WriteInt32(Writer, MD3Model.NumberOfSurfaces);
            WriteInt32(Writer, MD3Model.NumberOfSkins);
            WriteInt32(Writer, OffsetOfFrames);
            WriteInt32(Writer, OffsetOfTags);
            WriteInt32(Writer, OffsetOfSurfaces);
            WriteInt32(Writer, OffsetEnd);
            // Write the frames.
            for (int FrameIndex = 0; FrameIndex < MD3Model.NumberOfFrames; FrameIndex++) {
                TMD3Frame MD3ModelFrame = MD3Model.Frames.get(FrameIndex);
                WriteFloat32(Writer, MD3ModelFrame.MinBound.X);
                WriteFloat32(Writer, MD3ModelFrame.MinBound.Y);
                WriteFloat32(Writer, MD3ModelFrame.MinBound.Z);
                WriteFloat32(Writer, MD3ModelFrame.MaxBound.X);
                WriteFloat32(Writer, MD3ModelFrame.MaxBound.Y);
                WriteFloat32(Writer, MD3ModelFrame.MaxBound.Z);
                WriteFloat32(Writer, MD3ModelFrame.LocalOrigin.X);
                WriteFloat32(Writer, MD3ModelFrame.LocalOrigin.Y);
                WriteFloat32(Writer, MD3ModelFrame.LocalOrigin.Z);
                WriteFloat32(Writer, MD3ModelFrame.Radius);
                WriteString(Writer, MD3ModelFrame.Name, 16);
            }
            // Write the tags.
            for (int FrameIndex = 0; FrameIndex < MD3Model.NumberOfFrames; FrameIndex++) {
                for (int TagIndex = 0; TagIndex < MD3Model.NumberOfTags; TagIndex++) {
                    TMD3TagFrame MD3Tag = MD3Model.TagFrames.get(FrameIndex * MD3Model.NumberOfTags + TagIndex);
                    WriteString(Writer, MD3Tag.Name, 64);
                    WriteFloat32(Writer, MD3Tag.Location.X);
                    WriteFloat32(Writer, MD3Tag.Location.Y);
                    WriteFloat32(Writer, MD3Tag.Location.Z);
                    WriteFloat32(Writer, MD3Tag.OrientationX.X);
                    WriteFloat32(Writer, MD3Tag.OrientationX.Y);
                    WriteFloat32(Writer, MD3Tag.OrientationX.Z);
                    WriteFloat32(Writer, MD3Tag.OrientationY.X);
                    WriteFloat32(Writer, MD3Tag.OrientationY.Y);
                    WriteFloat32(Writer, MD3Tag.OrientationY.Z);
                    WriteFloat32(Writer, MD3Tag.OrientationZ.X);
                    WriteFloat32(Writer, MD3Tag.OrientationZ.Y);
                    WriteFloat32(Writer, MD3Tag.OrientationZ.Z);
                }
            }
            // Write the surfaces.
            for (int SurfaceIndex = 0; SurfaceIndex < MD3Model.NumberOfSurfaces; SurfaceIndex++) {
                TMD3Surface MD3Surface = MD3Model.Surfaces.get(SurfaceIndex);
                int OffsetOfShaders = 108;
                int OffsetOfTriangles = OffsetOfShaders + MD3Surface.MD3Shaders.size() * 68;
                int OffsetOfTextVerts = OffsetOfTriangles + MD3Surface.MD3Triangles.size() * 12;
                int OffsetOfVertices = OffsetOfTextVerts + MD3Surface.MD3TextVertexes.size() * 8;
                int SurfaceOffsetEnd = OffsetOfVertices + MD3Surface.MD3TextVertexes.size() * MD3Model.Frames.size() * 8;
                WriteString(Writer, MD3Surface.SurfaceID, 4);
                WriteString(Writer, MD3Surface.SurfaceName, 64);
                WriteInt32(Writer, MD3Surface.Flags);
                WriteInt32(Writer, MD3Model.NumberOfFrames);
                WriteInt32(Writer, MD3Surface.MD3Shaders.size());
                WriteInt32(Writer, MD3Surface.MD3TextVertexes.size());
                WriteInt32(Writer, MD3Surface.MD3Triangles.size());
                WriteInt32(Writer, OffsetOfTriangles);
                WriteInt32(Writer, OffsetOfShaders);
                WriteInt32(Writer, OffsetOfTextVerts);
                WriteInt32(Writer, OffsetOfVertices);
                WriteInt32(Writer, SurfaceOffsetEnd);
                // Write the shaders.
                for (int ShaderIndex = 0; ShaderIndex < MD3Surface.MD3Shaders.size(); ShaderIndex++) {
                    TMD3Shader MD3Shader = MD3Surface.MD3Shaders.get(ShaderIndex);
                    WriteString(Writer, MD3Shader.Name, 64);
                    WriteInt32(Writer, MD3Shader.Index);
                }
                // Write the triangles.
                for (int TriangleIndex = 0; TriangleIndex < MD3Surface.MD3Triangles.size(); TriangleIndex++) {
                    TMD3Triangle MD3Triangle = MD3Surface.MD3Triangles.get(TriangleIndex);
                    WriteInt32(Writer, MD3Triangle.I0);
                    WriteInt32(Writer, MD3Triangle.I1);
                    WriteInt32(Writer, MD3Triangle.I2);
                }
                // Read the texture coordinates.
                for (int VerticeIndex = 0; VerticeIndex < MD3Surface.MD3TextVertexes.size(); VerticeIndex++) {
                    TMD3TextVertex MD3TextVertex = MD3Surface.MD3TextVertexes.get(VerticeIndex);
                    WriteFloat32(Writer, MD3TextVertex.U);
                    WriteFloat32(Writer, MD3TextVertex.V);
                }
                // Read the space coordinates.
                for (int VerticeIndex = 0; VerticeIndex < MD3Surface.MD3TextVertexes.size() * MD3Model.Frames.size(); VerticeIndex++) {
                    TMD3Vertex MD3Vertex = MD3Surface.MD3Vertexes.get(VerticeIndex);
                    WriteInt16(Writer, MD3Vertex.X);
                    WriteInt16(Writer, MD3Vertex.Y);
                    WriteInt16(Writer, MD3Vertex.Z);
                    Writer.write(MD3Vertex.NormalLatitude);
                    Writer.write(MD3Vertex.NormalLongitude);
                }
            }
            Writer.flush();
        } catch (IOException E) {
            return false;
        }
        return true;
    }
    private static boolean SaveToText(TMD3Model MD3Model, String FileName) {
        StringBuilder SB = new StringBuilder();
        DecimalFormatSymbols DFS = new DecimalFormatSymbols();
        DFS.setDecimalSeparator('.');
        DecimalFormat DF = new DecimalFormat("#.#######", DFS);
        SB.append("# MD3 Export");
        SB.append(String.format("\n\nname %s", MD3Model.HeaderName));
        SB.append("\n\n# Frames");
        for (int I = 0; I < MD3Model.NumberOfFrames; I++) {
            TMD3Frame MD3ModelFrame = MD3Model.Frames.get(I);
            SB.append(String.format("\nframe %s", MD3ModelFrame.Name));
        }
        SB.append("\n\n# Tags");
        for (int I = 0; I < MD3Model.NumberOfTags; I++) {
            TMD3Tag MD3Tag = MD3Model.Tags.get(I);
            SB.append(String.format("\ntag %s", MD3Tag.Name));
        }
        for (int I = 0; I < MD3Model.NumberOfFrames; I++) {
            for (int J = 0; J < MD3Model.NumberOfTags; J++) {
                TMD3TagFrame MD3FrameTag = MD3Model.TagFrames.get(I * MD3Model.NumberOfTags + J);
                SB.append(String.format("\n\n# %d.Frame, %d.Tag %s", I + 1, J + 1, MD3FrameTag.Name));
                SB.append(String.format("\ntp %s %s %s", DF.format(MD3FrameTag.Location.X), DF.format(MD3FrameTag.Location.Y), DF.format(MD3FrameTag.Location.Z)));
                SB.append(String.format("\ntr %s %s %s %s %s %s %s %s %s",
                        DF.format(MD3FrameTag.OrientationX.X), DF.format(MD3FrameTag.OrientationX.Y), DF.format(MD3FrameTag.OrientationX.Z),
                        DF.format(MD3FrameTag.OrientationY.X), DF.format(MD3FrameTag.OrientationY.Y), DF.format(MD3FrameTag.OrientationY.Z),
                        DF.format(MD3FrameTag.OrientationZ.X), DF.format(MD3FrameTag.OrientationZ.Y), DF.format(MD3FrameTag.OrientationZ.Z)));
            }
        }
        SB.append("\n\n# Surfaces");
        for (int I = 0; I < MD3Model.NumberOfSurfaces; I++) {
            SB.append(String.format("\n\n# %d.Surface", I + 1));
            TMD3Surface MD3Surface = MD3Model.Surfaces.get(I);
            SB.append(String.format("\nmesh %s", MD3Surface.SurfaceName));
            for (int J = 0; J < MD3Surface.MD3Shaders.size(); J++)
                SB.append(String.format("\nmaterial %s", MD3Surface.MD3Shaders.get(J).Name));
            SB.append("\n");
            for (int J = 0; J < MD3Surface.MD3TextVertexes.size(); J++) {
                TMD3TextVertex MD3TextVertex = MD3Surface.MD3TextVertexes.get(J);
                SB.append(String.format("\nvt %s %s", DF.format(MD3TextVertex.U), DF.format(MD3TextVertex.V)));
            }
            for (int J = 0; J < MD3Surface.MD3Triangles.size(); J++) {
                TMD3Triangle MD3Triangle = MD3Surface.MD3Triangles.get(J);
                SB.append(String.format("\nfm %d %d %d", MD3Triangle.I0, MD3Triangle.I1, MD3Triangle.I2));
            }
            int VertexIndex = 0;
            for (int FrameIndex = 0; FrameIndex < MD3Model.Frames.size(); FrameIndex++) {
                SB.append(String.format("\n\n# %d.Frame", FrameIndex + 1));
                for (int J = 0; J < MD3Surface.MD3TextVertexes.size(); J++) {
                    TMD3Vertex MD3Vertex = MD3Surface.MD3Vertexes.get(VertexIndex);
                    SB.append(String.format("\nvp %s %s %s %d %d", DF.format(MD3Vertex.X), DF.format(MD3Vertex.Y), DF.format(MD3Vertex.Z), MD3Vertex.NormalLatitude, MD3Vertex.NormalLongitude));
                    VertexIndex++;
                }
            }
        }
        SB.append("\n\n# End Of File\n");
        try (FileWriter Writer = new FileWriter(FileName)) {
            Writer.write(SB.toString());
            Writer.flush();
        } catch (IOException Ignored) {
            return false;
        }
        return true;
    }
    private static void ShowUsage() {
        System.out.println("Usage: Main.class <input file> [output file]");
        System.out.println("<input file> must be specified with the extension, .MD3 or .ED3;");
        System.out.println("[output file] may be specified without extension;");
        System.out.println("if no output extension is specified, then the other than <input file> will be used.");
    }
    public static void main(String[] args) {
        if ((args.length == 0) || (args.length > 2)) {
            ShowUsage();
            System.exit(1);
        }
        String InputFileSpec = args[0];
        if ((!InputFileSpec.toUpperCase().endsWith(".MD3")) && (!InputFileSpec.toUpperCase().endsWith(".ED3"))) {
            ShowUsage();
            System.exit(1);
        }
        String OutputFileSpec = null;
        if (args.length == 2) {
            OutputFileSpec = args[1];
            if ((OutputFileSpec.indexOf('.') > 0) && (!OutputFileSpec.toUpperCase().endsWith(".MD3")) && (!OutputFileSpec.toUpperCase().endsWith(".ED3"))) {
                ShowUsage();
                System.exit(1);
            }
        }
        TMD3Model MD3Model = new TMD3Model();
        File InputFile = new File(InputFileSpec);
        if ((!InputFile.exists()) || (!InputFile.isFile())) {
            System.out.println(String.format("Couldn't find input file \"%s\"!", InputFileSpec));
            System.exit(2);
        }
        if (InputFileSpec.toUpperCase().endsWith(".MD3")) {
            if (OutputFileSpec == null)
                OutputFileSpec = InputFileSpec.substring(0, InputFileSpec.lastIndexOf('.')) + ".ED3";
            if (!LoadFromBinary(MD3Model, InputFileSpec)) {
                System.out.println(String.format("Couldn't load model from file \"%s\"!", InputFileSpec));
                System.exit(3);
            }
        } else {
            if (OutputFileSpec == null)
                OutputFileSpec = InputFileSpec.substring(0, InputFileSpec.lastIndexOf('.')) + ".MD3";
            if (!LoadFromText(MD3Model, InputFileSpec)) {
                System.out.println(String.format("Couldn't load export model from file \"%s\"!", InputFileSpec));
                System.exit(3);
            }
        }
        File OutputFile = new File(OutputFileSpec);
        if (OutputFile.exists()) {
            System.out.println(String.format("Output file \"%s\" already exists!", OutputFileSpec));
            System.exit(4);
        }
        if (OutputFileSpec.toUpperCase().endsWith(".MD3")) {
            if (!SaveToBinary(MD3Model, OutputFileSpec)) {
                System.out.println(String.format("An error has occured writing model \"%s\"!", OutputFileSpec));
                System.exit(5);
            }
        } else {
            if (!SaveToText(MD3Model, OutputFileSpec)) {
                System.out.println(String.format("An error has occured writing export model \"%s\"!", OutputFileSpec));
                System.exit(5);
            }
        }
    }
}
